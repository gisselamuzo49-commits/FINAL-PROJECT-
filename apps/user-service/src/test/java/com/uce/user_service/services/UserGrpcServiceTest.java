package com.uce.user_service.services;

import com.uce.user_service.grpc.StudentInfo;
import com.uce.user_service.grpc.StudentRequest;
import com.uce.user_service.models.UserProfile;
import com.uce.user_service.repositories.UserProfileRepository;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserGrpcServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserGrpcService userGrpcService;

    @Mock
    private StreamObserver<StudentInfo> responseObserver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getStudentInfo_WhenStudentExists_ReturnsFoundTrue() {
        // Arrange
        String studentId = "1";
        UserProfile profile = new UserProfile();
        profile.setId(1L);
        profile.setFirstName("Juan");
        profile.setLastName("Perez");
        profile.setCarrera("Sistemas");

        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        StudentRequest request = StudentRequest.newBuilder()
                .setEstudianteId(studentId)
                .build();

        // Act
        userGrpcService.getStudentInfo(request, responseObserver);

        // Assert
        ArgumentCaptor<StudentInfo> responseCaptor = ArgumentCaptor.forClass(StudentInfo.class);
        verify(responseObserver, times(1)).onNext(responseCaptor.capture());
        verify(responseObserver, times(1)).onCompleted();

        StudentInfo response = responseCaptor.getValue();
        assertTrue(response.getEncontrado());
        assertEquals("1", response.getId());
        assertEquals("Juan", response.getNombre());
        assertEquals("Perez", response.getApellido());
        assertEquals("Sistemas", response.getCarrera());
    }

    @Test
    void getStudentInfo_WhenStudentDoesNotExist_ReturnsFoundFalse() {
        // Arrange
        String studentId = "2";
        when(userProfileRepository.findById(2L)).thenReturn(Optional.empty());

        StudentRequest request = StudentRequest.newBuilder()
                .setEstudianteId(studentId)
                .build();

        // Act
        userGrpcService.getStudentInfo(request, responseObserver);

        // Assert
        ArgumentCaptor<StudentInfo> responseCaptor = ArgumentCaptor.forClass(StudentInfo.class);
        verify(responseObserver, times(1)).onNext(responseCaptor.capture());
        verify(responseObserver, times(1)).onCompleted();

        StudentInfo response = responseCaptor.getValue();
        assertFalse(response.getEncontrado());
    }

    @Test
    void getStudentInfo_WhenInvalidIdFormat_ReturnsFoundFalse() {
        // Arrange
        String studentId = "invalid-id";

        StudentRequest request = StudentRequest.newBuilder()
                .setEstudianteId(studentId)
                .build();

        // Act
        userGrpcService.getStudentInfo(request, responseObserver);

        // Assert
        ArgumentCaptor<StudentInfo> responseCaptor = ArgumentCaptor.forClass(StudentInfo.class);
        verify(responseObserver, times(1)).onNext(responseCaptor.capture());
        verify(responseObserver, times(1)).onCompleted();

        StudentInfo response = responseCaptor.getValue();
        assertFalse(response.getEncontrado());
    }
}
