package com.uce.document_service.services;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.uce.document_service.models.DocumentoGenerado;
import com.uce.document_service.models.DocumentoResumen;
import com.uce.document_service.models.EstadoDocumento;
import com.uce.document_service.models.TipoDocumento;
import com.uce.document_service.repositories.DocumentoGeneradoRepository;
import com.uce.document_service.repositories.DocumentoResumenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    @Autowired
    private DocumentoGeneradoRepository postgresRepository;

    @Autowired
    private DocumentoResumenRepository mongoRepository;

    @Autowired
    private Optional<S3Client> s3Client;

    @Value("${s3.bucket.name:}")
    private String bucketName;

    @Value("${n8n.webhook.url:}")
    private String n8nWebhookUrl;

    private final RestTemplate restTemplate = new RestTemplate(new org.springframework.http.client.JdkClientHttpRequestFactory());

    @Transactional
    public DocumentoGenerado generateAndUploadDocument(String estudianteId, String proyectoId, Double horas, String fecha, Long horasId) {
        logger.info("Iniciando generación de documento para estudiante: {}, proyecto: {}", estudianteId, proyectoId);
        
        byte[] pdfBytes = null;
        try {
            pdfBytes = generatePdfBytes(estudianteId, proyectoId, horas, fecha);
        } catch (Exception e) {
            logger.error("Error al generar el archivo PDF: {}", e.getMessage(), e);
            return saveErrorMetadata(estudianteId, proyectoId);
        }

        String s3Key = "documents/" + estudianteId + "_" + horasId + ".pdf";
        String s3Url = null;
        EstadoDocumento estado = EstadoDocumento.ERROR;

        if (s3Client.isPresent() && bucketName != null && !bucketName.trim().isEmpty()) {
            try {
                logger.info("Subiendo PDF a S3 bucket: {}, key: {}", bucketName, s3Key);
                PutObjectRequest putOb = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .contentType("application/pdf")
                        .build();

                s3Client.get().putObject(putOb, RequestBody.fromBytes(pdfBytes));
                s3Url = "https://" + bucketName + ".s3.amazonaws.com/" + s3Key;
                estado = EstadoDocumento.GENERADO;
                logger.info("PDF subido exitosamente. URL: {}", s3Url);
            } catch (Exception e) {
                logger.error("Error al subir el archivo a S3 (flujo best-effort continuará con estado ERROR): {}", e.getMessage());
                // No relanzamos la excepción
            }
        } else {
            logger.warn("S3Client no está configurado o S3_BUCKET_NAME está vacío. El documento se marcará con estado ERROR.");
        }

        // 1. Guardar metadata en PostgreSQL
        DocumentoGenerado doc = new DocumentoGenerado(
                estudianteId,
                proyectoId,
                TipoDocumento.CERTIFICADO_HORAS,
                s3Key,
                s3Url,
                estado
        );
        DocumentoGenerado savedDoc = postgresRepository.save(doc);

        // 2. Actualizar read model en MongoDB (upsert)
        try {
            updateMongoReadModel(savedDoc);
        } catch (Exception e) {
            logger.error("Error al actualizar la proyección de lectura en MongoDB: {}", e.getMessage(), e);
        }

        // 3. Disparar Webhook a n8n (best-effort)
        if (estado == EstadoDocumento.GENERADO && s3Url != null) {
            triggerWebhook(estudianteId, s3Url, TipoDocumento.CERTIFICADO_HORAS.toString());
        }

        return savedDoc;
    }

    private byte[] generatePdfBytes(String estudianteId, String proyectoId, Double horas, String fecha) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document pdfDoc = new Document();
        PdfWriter.getInstance(pdfDoc, baos);
        pdfDoc.open();
        pdfDoc.add(new Paragraph("Certificado de Horas UCE"));
        pdfDoc.add(new Paragraph("----------------------------------"));
        pdfDoc.add(new Paragraph("Estudiante ID: " + estudianteId));
        pdfDoc.add(new Paragraph("Proyecto ID: " + proyectoId));
        pdfDoc.add(new Paragraph("Horas Registradas: " + horas));
        pdfDoc.add(new Paragraph("Fecha de Actividad: " + (fecha != null ? fecha : "N/A")));
        pdfDoc.add(new Paragraph("Estado de Registro: VALIDADO"));
        pdfDoc.add(new Paragraph("----------------------------------"));
        pdfDoc.add(new Paragraph("Este documento certifica que las horas indicadas han sido validadas por el tutor correspondiente."));
        pdfDoc.close();
        return baos.toByteArray();
    }

    private DocumentoGenerado saveErrorMetadata(String estudianteId, String proyectoId) {
        DocumentoGenerado doc = new DocumentoGenerado(
                estudianteId,
                proyectoId,
                TipoDocumento.CERTIFICADO_HORAS,
                null,
                null,
                EstadoDocumento.ERROR
        );
        return postgresRepository.save(doc);
    }

    private void updateMongoReadModel(DocumentoGenerado doc) {
        DocumentoResumen resumen = mongoRepository.findById(doc.getEstudianteId())
                .orElseGet(() -> new DocumentoResumen(doc.getEstudianteId()));

        DocumentoResumen.DocumentInfo docInfo = new DocumentoResumen.DocumentInfo(
                doc.getId(),
                doc.getTipo().toString(),
                doc.getS3Url(),
                doc.getCreatedAt() != null ? doc.getCreatedAt() : LocalDateTime.now()
        );

        resumen.getDocumentos().add(docInfo);
        resumen.setTotalDocumentos(resumen.getDocumentos().size());

        mongoRepository.save(resumen);
        logger.info("Proyección en MongoDB actualizada para estudiante: {}", doc.getEstudianteId());
    }

    private void triggerWebhook(String estudianteId, String s3Url, String tipo) {
        if (n8nWebhookUrl == null || n8nWebhookUrl.trim().isEmpty()) {
            logger.info("N8N_WEBHOOK_URL no configurada. Saltando envío de webhook.");
            return;
        }

        logger.info("Enviando webhook a n8n: {}", n8nWebhookUrl);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> payload = new HashMap<>();
            payload.put("estudianteId", estudianteId);
            payload.put("s3Url", s3Url);
            payload.put("tipo", tipo);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForLocation(n8nWebhookUrl, request);
            logger.info("Webhook enviado exitosamente a n8n.");
        } catch (Exception e) {
            logger.error("Error al enviar webhook a n8n (flujo best-effort continuará): {}", e.getMessage());
        }
    }
}
