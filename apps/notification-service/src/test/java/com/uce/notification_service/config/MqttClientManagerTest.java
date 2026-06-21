package com.uce.notification_service.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MqttClientManagerTest {

    private MqttClient mockMqttClient;
    private MqttClientManager clientManager;

    @BeforeEach
    void setUp() {
        mockMqttClient = mock(MqttClient.class);

        // Configure a fast Circuit Breaker for test purposes to avoid slow Thread.sleep
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(4)
                .minimumNumberOfCalls(2)
                .waitDurationInOpenState(Duration.ofMillis(100)) // 100ms wait
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();

        clientManager = new MqttClientManager(config);
        clientManager.setMqttClient(mockMqttClient);
    }

    @Test
    void whenMqttPublishSucceeds_stateIsClosed() throws MqttException {
        when(mockMqttClient.isConnected()).thenReturn(true);
        doNothing().when(mockMqttClient).publish(any(), any());

        clientManager.publish("test/topic", "test payload");

        assertEquals("CLOSED", clientManager.getCircuitBreakerState());
        verify(mockMqttClient, times(1)).publish(eq("test/topic"), any());
    }

    @Test
    void whenMqttPublishFailsRepeatedly_circuitBreakerOpensAndShortCircuits() throws MqttException, InterruptedException {
        // Setup mock client to be connected but fail on publish
        when(mockMqttClient.isConnected()).thenReturn(true);
        doThrow(new MqttException(MqttException.REASON_CODE_CLIENT_EXCEPTION))
                .when(mockMqttClient).publish(any(), any());

        // Circuit breaker is CLOSED initially
        assertEquals("CLOSED", clientManager.getCircuitBreakerState());

        // Make first failed call
        clientManager.publish("test/topic", "payload 1");

        // Make second failed call
        clientManager.publish("test/topic", "payload 2");

        // Circuit breaker should now be OPEN because minimumNumberOfCalls (2) met and 100% failure rate
        assertEquals("OPEN", clientManager.getCircuitBreakerState());

        // The third call should be short-circuited (mockMqttClient.publish not called again)
        reset(mockMqttClient);
        when(mockMqttClient.isConnected()).thenReturn(true);
        clientManager.publish("test/topic", "payload 3");
        verify(mockMqttClient, never()).publish(any(), any());

        // Wait 150ms for waitDurationInOpenState (100ms) to elapse
        Thread.sleep(150);

        // Next call should transition state to HALF_OPEN and invoke mockMqttClient
        doNothing().when(mockMqttClient).publish(any(), any());

        clientManager.publish("test/topic", "payload 4");

        // In HALF_OPEN state, we configured permittedNumberOfCallsInHalfOpenState = 2.
        // Let's make one more successful call to close the circuit breaker
        clientManager.publish("test/topic", "payload 5");

        // Now Circuit Breaker should be CLOSED again
        assertEquals("CLOSED", clientManager.getCircuitBreakerState());
    }
}
