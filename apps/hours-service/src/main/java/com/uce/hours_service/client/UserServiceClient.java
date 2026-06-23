package com.uce.hours_service.client;

import java.util.Optional;

public interface UserServiceClient {
    Optional<StudentInfo> getStudentInfo(String estudianteId);
}
