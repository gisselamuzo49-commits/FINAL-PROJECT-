package com.uce.notification_service.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;

@Component
public class MqttClientManager {

    private static final Logger logger = LoggerFactory.getLogger(MqttClientManager.class);

    @Value("${mqtt.broker.host}")
    private String host;

    @Value("${mqtt.broker.port:1883}")
    private int port;

    @Value("${mqtt.broker.username:}")
    private String username;

    @Value("${mqtt.broker.password:}")
    private String password;

    @Value("${mqtt.broker.client-id:notification-service-client}")
    private String clientId;

    private MqttClient mqttClient;
    private boolean connected = false;

    private final CircuitBreaker circuitBreaker;

    public MqttClientManager() {
        this(CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 50% failure rate opens the circuit
                .slidingWindowSize(10)
                .waitDurationInOpenState(Duration.ofSeconds(15))
                .permittedNumberOfCallsInHalfOpenState(3)
                .build());
    }

    public MqttClientManager(CircuitBreakerConfig customConfig) {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(customConfig);
        this.circuitBreaker = registry.circuitBreaker("mqttPublisher");
    }

    @PostConstruct
    public void init() {
        connect();
    }

    public synchronized void connect() {
        if (mqttClient != null && mqttClient.isConnected()) {
            return;
        }

        String protocol = (port == 8883) ? "ssl://" : "tcp://";
        String brokerUrl = protocol + host + ":" + port;
        logger.info("Intentando conectar al broker MQTT: {} con Client ID: {}", brokerUrl, clientId);

        try {
            mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setAutomaticReconnect(true);
            connOpts.setConnectionTimeout(10);
            connOpts.setKeepAliveInterval(60);

            if (username != null && !username.trim().isEmpty()) {
                connOpts.setUserName(username);
            }
            if (password != null && !password.trim().isEmpty()) {
                connOpts.setPassword(password.toCharArray());
            }

            mqttClient.connect(connOpts);
            connected = true;
            logger.info("Conexión MQTT establecida exitosamente con el broker.");
        } catch (MqttException e) {
            logger.error("No se pudo conectar al broker MQTT en la inicialización (el broker podría estar offline o inaccesible): {}. Detalles: {}", brokerUrl, e.getMessage());
            // No propagamos la excepción para evitar que falle el arranque de la app si no hay internet o en tests locales
            connected = false;
        }
    }

    public synchronized void publish(String topic, String payload) {
        try {
            circuitBreaker.executeRunnable(() -> {
                if (mqttClient == null || !mqttClient.isConnected()) {
                    logger.warn("El cliente MQTT no está conectado. Intentando reconectar antes de publicar...");
                    connect();
                }

                if (mqttClient != null && mqttClient.isConnected()) {
                    try {
                        MqttMessage message = new MqttMessage(payload.getBytes());
                        message.setQos(1); // At least once delivery
                        mqttClient.publish(topic, message);
                        logger.info("Mensaje publicado en topic MQTT [{}]: {}", topic, payload);
                    } catch (MqttException e) {
                        throw new RuntimeException("Error en publicación MQTT: " + e.getMessage(), e);
                    }
                } else {
                    throw new RuntimeException("No se pudo publicar el mensaje porque el cliente MQTT no está conectado.");
                }
            });
        } catch (Exception e) {
            logger.error("Error al publicar mensaje en el topic [{}] vía Circuit Breaker: {}", topic, e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                mqttClient.close();
                logger.info("Cliente MQTT desconectado limpiamente.");
            } catch (MqttException e) {
                logger.error("Error al desconectar cliente MQTT: {}", e.getMessage());
            }
        }
    }

    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    public String getCircuitBreakerState() {
        return circuitBreaker.getState().name();
    }

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    // Setter to allow mock injection during unit tests
    void setMqttClient(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }
}
