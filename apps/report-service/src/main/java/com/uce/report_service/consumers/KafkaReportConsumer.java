package com.uce.report_service.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uce.report_service.models.RegistroHorasReporte;
import com.uce.report_service.models.ReporteEstudiante;
import com.uce.report_service.models.ReporteGlobal;
import com.uce.report_service.repositories.RegistroHorasReporteRepository;
import com.uce.report_service.repositories.ReporteEstudianteRepository;
import com.uce.report_service.repositories.ReporteGlobalRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class KafkaReportConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaReportConsumer.class);

    @Autowired
    private RegistroHorasReporteRepository registroRepository;

    @Autowired
    private ReporteEstudianteRepository studentRepository;

    @Autowired
    private ReporteGlobalRepository globalRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${document.service.url}")
    private String documentServiceUrl;

    private CircuitBreaker circuitBreaker;

    public KafkaReportConsumer() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 50% failure rate opens the circuit
                .slidingWindowSize(10)
                .waitDurationInOpenState(Duration.ofSeconds(15))
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        this.circuitBreaker = registry.circuitBreaker("documentService");
    }

    @KafkaListener(topics = "horas.registradas", groupId = "report-service-group")
    public void consume(String message) {
        log.info("Received event from Kafka: {}", message);
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String id = (String) event.get("id");
            String estudianteId = (String) event.get("estudianteId");
            String estado = (String) event.get("estado");
            
            Object horasObj = event.get("horas");
            BigDecimal horas = BigDecimal.ZERO;
            if (horasObj != null) {
                horas = new BigDecimal(horasObj.toString());
            }

            if (id == null || estudianteId == null || estado == null) {
                throw new IllegalArgumentException("Kafka event is missing id, estudianteId, or estado");
            }

            // 1. Upsert RegistroHorasReporte in PostgreSQL
            RegistroHorasReporte registro = new RegistroHorasReporte(id, estudianteId, horas, estado);
            registroRepository.save(registro);

            // 2. Recalculate totals for the student
            List<RegistroHorasReporte> studentRegistrations = registroRepository.findByEstudianteId(estudianteId);
            BigDecimal totalValidadas = BigDecimal.ZERO;
            BigDecimal totalPendientes = BigDecimal.ZERO;

            for (RegistroHorasReporte r : studentRegistrations) {
                if ("VALIDADO".equalsIgnoreCase(r.getEstado())) {
                    totalValidadas = totalValidadas.add(r.getHoras());
                } else if ("PENDIENTE".equalsIgnoreCase(r.getEstado())) {
                    totalPendientes = totalPendientes.add(r.getHoras());
                }
            }

            // 3. Fetch documents count from document-service (best effort)
            int totalDocumentos = 0;
            try {
                final String url = documentServiceUrl + "/api/documents/student/" + estudianteId;
                log.info("Querying document-service at: {}", url);
                Map<?, ?> response = circuitBreaker.executeSupplier(() -> restTemplate.getForObject(url, Map.class));
                if (response != null && response.containsKey("totalDocumentos")) {
                    totalDocumentos = ((Number) response.get("totalDocumentos")).intValue();
                }
            } catch (Exception e) {
                log.warn("Could not retrieve documents count from document-service via Circuit Breaker for student {}: {}", estudianteId, e.getMessage());
            }

            // 4. Save/Update ReporteEstudiante in PostgreSQL
            Optional<ReporteEstudiante> existingReportOpt = studentRepository.findByEstudianteId(estudianteId);
            ReporteEstudiante studentReport;
            if (existingReportOpt.isPresent()) {
                studentReport = existingReportOpt.get();
                studentReport.setTotalHorasValidadas(totalValidadas);
                studentReport.setTotalHorasPendientes(totalPendientes);
                studentReport.setTotalDocumentos(totalDocumentos);
                studentReport.setUltimaActualizacion(LocalDateTime.now());
            } else {
                studentReport = new ReporteEstudiante(estudianteId, totalValidadas, totalPendientes, totalDocumentos, LocalDateTime.now());
            }
            studentRepository.save(studentReport);

            // 5. Recalculate global statistics for MongoDB
            recalculateGlobalReport();

        } catch (Exception e) {
            log.error("Error processing event message: {}", message, e);
            throw new IllegalStateException("Unable to process Kafka report event", e);
        }
    }

    public void recalculateGlobalReport() {
        log.info("Recalculating global report...");
        List<ReporteEstudiante> allReports = studentRepository.findAll();
        
        int totalEstudiantes = allReports.size();
        BigDecimal totalValidadas = BigDecimal.ZERO;
        BigDecimal totalPendientes = BigDecimal.ZERO;
        
        for (ReporteEstudiante r : allReports) {
            totalValidadas = totalValidadas.add(r.getTotalHorasValidadas());
            totalPendientes = totalPendientes.add(r.getTotalHorasPendientes());
        }

        Map<String, Integer> facultadMap = new HashMap<>();
        facultadMap.put("FICA", totalEstudiantes); // Placeholder

        ReporteGlobal globalReport = new ReporteGlobal(totalEstudiantes, totalValidadas, totalPendientes, facultadMap, LocalDateTime.now());
        globalRepository.save(globalReport);
        log.info("Global report updated in MongoDB successfully.");
    }

    public String getCircuitBreakerState() {
        return circuitBreaker.getState().name();
    }

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    // Setters for test mock injection
    void setRegistroRepository(RegistroHorasReporteRepository registroRepository) {
        this.registroRepository = registroRepository;
    }

    void setStudentRepository(ReporteEstudianteRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    void setGlobalRepository(ReporteGlobalRepository globalRepository) {
        this.globalRepository = globalRepository;
    }

    void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    void setDocumentServiceUrl(String documentServiceUrl) {
        this.documentServiceUrl = documentServiceUrl;
    }

    void setCircuitBreaker(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }
}
