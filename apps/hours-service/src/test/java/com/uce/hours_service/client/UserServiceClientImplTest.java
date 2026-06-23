package com.uce.hours_service.client;

import com.uce.user_service.grpc.StudentRequest;
import com.uce.user_service.grpc.UserServiceGrpc;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceClientImplTest {

    private UserServiceGrpc.UserServiceBlockingStub mockStub;
    private UserServiceClientImpl client;

    @BeforeEach
    void setUp() {
        mockStub = mock(UserServiceGrpc.UserServiceBlockingStub.class);

        // Configure a fast Circuit Breaker for test purposes to avoid slow Thread.sleep
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(4)
                .minimumNumberOfCalls(2)
                .waitDurationInOpenState(Duration.ofMillis(100)) // 100ms wait
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();

        client = new UserServiceClientImpl(config);
        client.setUserServiceStub(mockStub);
    }

    @Test
    void whenGrpcCallSucceeds_returnsStudentInfoAndStateIsClosed() {
        com.uce.user_service.grpc.StudentInfo mockResponse = com.uce.user_service.grpc.StudentInfo.newBuilder()
                .setEncontrado(true)
                .setNombre("Juan")
                .setApellido("Perez")
                .setCarrera("Sistemas")
                .build();

        when(mockStub.withDeadlineAfter(anyLong(), any())).thenReturn(mockStub);
        when(mockStub.getStudentInfo(any(StudentRequest.class))).thenReturn(mockResponse);

        Optional<StudentInfo> result = client.getStudentInfo("123");

        assertTrue(result.isPresent());
        assertEquals("Juan", result.get().getNombre());
        assertEquals("Sistemas", result.get().getCarrera());
        assertEquals("CLOSED", client.getCircuitBreakerState());
    }

    @Test
    void whenGrpcCallFailsRepeatedly_circuitBreakerOpensAndShortCircuits() throws InterruptedException {
        // Setup mock stub to fail
        when(mockStub.withDeadlineAfter(anyLong(), any())).thenReturn(mockStub);
        when(mockStub.getStudentInfo(any(StudentRequest.class)))
                .thenThrow(new StatusRuntimeException(Status.UNAVAILABLE));

        // Circuit breaker is CLOSED initially
        assertEquals("CLOSED", client.getCircuitBreakerState());

        // Make first failed call
        Optional<StudentInfo> res1 = client.getStudentInfo("123");
        assertFalse(res1.isPresent());

        // Make second failed call
        Optional<StudentInfo> res2 = client.getStudentInfo("123");
        assertFalse(res2.isPresent());

        // Circuit breaker should now be OPEN because minimumNumberOfCalls (2) met and 100% failure rate
        assertEquals("OPEN", client.getCircuitBreakerState());

        // The third call should be short-circuited (mockStub not called again)
        reset(mockStub);
        Optional<StudentInfo> res3 = client.getStudentInfo("123");
        assertFalse(res3.isPresent());
        verifyNoInteractions(mockStub);

        // Wait 150ms for waitDurationInOpenState (100ms) to elapse
        Thread.sleep(150);

        // Next call should transition state to HALF_OPEN and invoke mockStub
        com.uce.user_service.grpc.StudentInfo mockSuccessResponse = com.uce.user_service.grpc.StudentInfo.newBuilder()
                .setEncontrado(true)
                .setNombre("Ana")
                .setApellido("Gomez")
                .setCarrera("Software")
                .build();

        when(mockStub.withDeadlineAfter(anyLong(), any())).thenReturn(mockStub);
        when(mockStub.getStudentInfo(any(StudentRequest.class))).thenReturn(mockSuccessResponse);

        Optional<StudentInfo> res4 = client.getStudentInfo("123");
        assertTrue(res4.isPresent());
        assertEquals("Ana", res4.get().getNombre());

        // In HALF_OPEN state, we configured permittedNumberOfCallsInHalfOpenState = 2.
        // Let's make one more successful call to close the circuit breaker
        Optional<StudentInfo> res5 = client.getStudentInfo("123");
        assertTrue(res5.isPresent());

        // Now Circuit Breaker should be CLOSED again
        assertEquals("CLOSED", client.getCircuitBreakerState());
    }
}
