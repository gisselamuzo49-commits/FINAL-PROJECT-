package com.uce.gateway_service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            logger.info("Routing request to: {}", request.getPath().value());

            // 0. Permitir solicitudes CORS preflight (OPTIONS) sin validación de token
            if (org.springframework.http.HttpMethod.OPTIONS.equals(request.getMethod())) {
                return chain.filter(exchange);
            }

            // 1. Verificar si existe la cabecera Authorization
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Falta cabecera de autenticación", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // 2. Validar el token y extraer Claims usando la API JJWT 0.13.0
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                JwtParser parser = Jwts.parser().verifyWith(key).build();
                Claims claims = parser.parseSignedClaims(token).getPayload();

                String roleClaim = claims.get("rol", String.class);
                if (roleClaim == null) {
                    roleClaim = claims.get("role", String.class);
                }
                final String rol = roleClaim == null ? "" : roleClaim;

                String userId = claims.get("id", String.class);

                // 3. Mutar la petición para agregar datos del usuario autenticado para consumo interno
                ServerHttpRequest mutatedRequest = request.mutate()
                        .headers(headers -> {
                            headers.set("X-Auth-User", claims.getSubject());
                            headers.set("X-Auth-Roles", rol);
                            if (userId != null) {
                                headers.set("X-Auth-User-Id", userId);
                            } else {
                                headers.remove("X-Auth-User-Id");
                            }
                        })
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                return onError(exchange, "Token no válido o expirado", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    public static class Config {
        // Parámetros de configuración opcionales si se requieren
    }
}
