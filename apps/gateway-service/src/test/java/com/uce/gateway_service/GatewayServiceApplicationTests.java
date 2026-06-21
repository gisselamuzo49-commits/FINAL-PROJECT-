package com.uce.gateway_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayServiceApplicationTests {

    @LocalServerPort
    private int port;

    @Test
    void whenRequestingWithoutToken_thenReturnsUnauthorized() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/hours"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        assertEquals(401, response.statusCode());
    }

    @Test
    void whenRequestingHealth_thenBypassesFilterAndDoesNotReturnUnauthorized() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/hours/health"))
                .GET()
                .build();

        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        // Bypasses JWT filter, so it should attempt to connect and fail (e.g. 500 or 504), but NOT 401
        assertNotEquals(401, response.statusCode());
    }
}
