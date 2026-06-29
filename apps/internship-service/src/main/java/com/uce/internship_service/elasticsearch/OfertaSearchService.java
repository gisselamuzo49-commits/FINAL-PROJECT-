package com.uce.internship_service.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class OfertaSearchService {

    private final ElasticsearchClient esClient;
    private final String index;

    @org.springframework.beans.factory.annotation.Autowired
    public OfertaSearchService(
        @Value("${elasticsearch.url}") String url,
        @Value("${elasticsearch.index}") String index
    ) throws Exception {
        this.index = index;
        RestClient restClient = RestClient.builder(
            HttpHost.create(url)
        ).build();
        ElasticsearchTransport transport = new RestClientTransport(
            restClient, new JacksonJsonpMapper()
        );
        this.esClient = new ElasticsearchClient(transport);
    }

    // Constructor para pruebas unitarias
    OfertaSearchService(ElasticsearchClient esClient, String index) {
        this.esClient = esClient;
        this.index = index;
    }

    public void indexOferta(String id, String titulo, String empresa, 
                             String descripcion) {
        try {
            Map<String, Object> doc = new HashMap<>();
            doc.put("titulo", titulo);
            doc.put("empresa", empresa);
            doc.put("descripcion", descripcion);
            doc.put("indexed_at", Instant.now().toString());

            esClient.index(i -> i
                .index(index)
                .id(id)
                .document(doc)
            );
        } catch (Exception e) {
            // Log error but don't fail the main operation
        }
    }

    public String getDatabaseInfo() {
        return "{\"database\": \"Elasticsearch\", \"index\": \"" + 
               index + "\", \"status\": \"connected\"}";
    }
}
