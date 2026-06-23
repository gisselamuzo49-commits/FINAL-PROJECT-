package com.uce.hours_service.client;

import com.uce.user_service.grpc.StudentRequest;
import com.uce.user_service.grpc.UserServiceGrpc;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class UserServiceClientImpl implements UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClientImpl.class);

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

    private final CircuitBreaker circuitBreaker;

    public UserServiceClientImpl() {
        this(CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 50% failure rate opens the circuit
                .slidingWindowSize(10)
                .waitDurationInOpenState(Duration.ofSeconds(15))
                .permittedNumberOfCallsInHalfOpenState(3)
                .build());
    }

    public UserServiceClientImpl(CircuitBreakerConfig customConfig) {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(customConfig);
        this.circuitBreaker = registry.circuitBreaker("userServiceGrpc");
    }

    @Override
    public Optional<StudentInfo> getStudentInfo(String estudianteId) {
        try {
            return circuitBreaker.executeSupplier(() -> {
                StudentRequest request = StudentRequest.newBuilder()
                        .setEstudianteId(estudianteId)
                        .build();

                com.uce.user_service.grpc.StudentInfo response = userServiceStub
                        .withDeadlineAfter(2, java.util.concurrent.TimeUnit.SECONDS)
                        .getStudentInfo(request);

                if (response != null && response.getEncontrado()) {
                    return Optional.of(new StudentInfo(
                            response.getNombre(),
                            response.getApellido(),
                            response.getCarrera()
                    ));
                }
                return Optional.empty();
            });
        } catch (Exception e) {
            log.error("Error calling user-service gRPC via Circuit Breaker: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public String getCircuitBreakerState() {
        return circuitBreaker.getState().name();
    }

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    // Setter to allow mock injection during unit tests
    void setUserServiceStub(UserServiceGrpc.UserServiceBlockingStub userServiceStub) {
        this.userServiceStub = userServiceStub;
    }
}
