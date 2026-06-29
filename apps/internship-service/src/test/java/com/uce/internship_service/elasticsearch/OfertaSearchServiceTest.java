package com.uce.internship_service.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OfertaSearchServiceTest {

    private ElasticsearchClient esClient;
    private OfertaSearchService searchService;
    private final String index = "pasantias-ofertas";

    @BeforeEach
    void setUp() {
        esClient = mock(ElasticsearchClient.class);
        searchService = new OfertaSearchService(esClient, index);
    }

    @Test
    void testGetDatabaseInfoReturnsCorrectJson() {
        String info = searchService.getDatabaseInfo();

        assertNotNull(info);
        assertTrue(info.contains("\"database\": \"Elasticsearch\""));
        assertTrue(info.contains("\"index\": \"" + index + "\""));
        assertTrue(info.contains("\"status\": \"connected\""));
    }

    @Test
    void testIndexOfertaDoesNotThrowException() {
        String id = "OF123";
        String titulo = "Pasantía Java";
        String empresa = "Empresa S.A.";
        String descripcion = "Desarrollo de microservicios";

        assertDoesNotThrow(() -> {
            searchService.indexOferta(id, titulo, empresa, descripcion);
        });
    }
}
