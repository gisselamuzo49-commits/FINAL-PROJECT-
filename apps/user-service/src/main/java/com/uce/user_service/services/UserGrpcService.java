package com.uce.user_service.services;

import com.uce.user_service.grpc.StudentInfo;
import com.uce.user_service.grpc.StudentRequest;
import com.uce.user_service.grpc.UserServiceGrpc;
import com.uce.user_service.models.UserProfile;
import com.uce.user_service.repositories.UserProfileRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Override
    public void getStudentInfo(StudentRequest request, StreamObserver<StudentInfo> responseObserver) {
        String estudianteIdStr = request.getEstudianteId();
        StudentInfo.Builder responseBuilder = StudentInfo.newBuilder();

        try {
            Long id = Long.parseLong(estudianteIdStr);
            Optional<UserProfile> profileOpt = userProfileRepository.findById(id);

            if (profileOpt.isPresent()) {
                UserProfile profile = profileOpt.get();
                responseBuilder.setId(profile.getId().toString())
                        .setNombre(profile.getFirstName() != null ? profile.getFirstName() : "")
                        .setApellido(profile.getLastName() != null ? profile.getLastName() : "")
                        .setCarrera(profile.getCarrera() != null ? profile.getCarrera() : "")
                        .setEncontrado(true);
            } else {
                responseBuilder.setEncontrado(false);
            }
        } catch (NumberFormatException e) {
            responseBuilder.setEncontrado(false);
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
