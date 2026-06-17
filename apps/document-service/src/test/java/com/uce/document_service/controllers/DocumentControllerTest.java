package com.uce.document_service.controllers;

import com.uce.document_service.DocumentServiceApplication;
import com.uce.document_service.models.DocumentoGenerado;
import com.uce.document_service.models.DocumentoResumen;
import com.uce.document_service.models.EstadoDocumento;
import com.uce.document_service.models.TipoDocumento;
import com.uce.document_service.repositories.DocumentoGeneradoRepository;
import com.uce.document_service.repositories.DocumentoResumenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DocumentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DocumentoGeneradoRepository postgresRepository;

    @Mock
    private DocumentoResumenRepository mongoRepository;

    @InjectMocks
    private DocumentController documentController;

    private final DocumentServiceApplication application = new DocumentServiceApplication();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(documentController, application).build();
    }

    @Test
    void testGetResumenStudentSuccess() throws Exception {
        DocumentoResumen mockResumen = new DocumentoResumen("student_1");
        mockResumen.setTotalDocumentos(1);
        mockResumen.getDocumentos().add(new DocumentoResumen.DocumentInfo(
                10L,
                TipoDocumento.CERTIFICADO_HORAS.toString(),
                "https://s3.url",
                LocalDateTime.now()
        ));

        Mockito.when(mongoRepository.findById("student_1"))
                .thenReturn(Optional.of(mockResumen));

        mockMvc.perform(get("/api/documents/student/student_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("student_1"))
                .andExpect(jsonPath("$.totalDocumentos").value(1))
                .andExpect(jsonPath("$.documentos[0].documentoId").value(10L));
    }

    @Test
    void testGetResumenStudentNotFound() throws Exception {
        Mockito.when(mongoRepository.findById("student_999"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/documents/student/student_999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetMetadataByIdSuccess() throws Exception {
        DocumentoGenerado doc = new DocumentoGenerado(
                "student_1",
                "proj_1",
                TipoDocumento.CERTIFICADO_HORAS,
                "key",
                "https://s3.url",
                EstadoDocumento.GENERADO
        );
        doc.setId(10L);

        Mockito.when(postgresRepository.findById(eq(10L)))
                .thenReturn(Optional.of(doc));

        mockMvc.perform(get("/api/documents/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.estudianteId").value("student_1"))
                .andExpect(jsonPath("$.estado").value("GENERADO"));
    }

    @Test
    void testGetMetadataByIdNotFound() throws Exception {
        Mockito.when(postgresRepository.findById(eq(999L)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/documents/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("document-service is running"));
    }
}
