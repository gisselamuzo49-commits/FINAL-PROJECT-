package com.uce.hours_service.cqrs;

import com.uce.hours_service.models.HorasResumen;
import com.uce.hours_service.repositories.HorasResumenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class HoursQueryService {

    @Autowired
    private HorasResumenRepository resumenRepository;

    public Optional<HorasResumen> getStudentSummary(String estudianteId) {
        return resumenRepository.findById(estudianteId);
    }
}
