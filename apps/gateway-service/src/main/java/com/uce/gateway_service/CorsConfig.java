package com.uce.gateway_service;

import org.springframework.context.annotation.Configuration;

/**
 * La configuración CORS está delegada a los microservicios individuales.
 * El Gateway NO agrega headers CORS propios para evitar duplicados.
 * Cada microservicio tiene su propio WebMvcConfigurer con allowedOrigins("*").
 */
@Configuration
public class CorsConfig {
    // CORS es manejado por los microservicios individuales (auth-service, internship-service, etc.)
    // No se define CorsWebFilter aquí para evitar headers Access-Control-Allow-Origin duplicados.
}
