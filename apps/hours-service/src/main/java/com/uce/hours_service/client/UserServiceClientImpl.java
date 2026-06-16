package com.uce.hours_service.client;

import com.uce.user_service.grpc.StudentRequest;
import com.uce.user_service.grpc.UserServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceClientImpl implements UserServiceClient {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

    @Override
    public Optional<StudentInfo> getStudentInfo(String estudianteId) {
        try {
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
        } catch (Exception e) {
            System.err.println("Error calling user-service gRPC: " + e.getMessage());
        }
        return Optional.empty();
    }
}
