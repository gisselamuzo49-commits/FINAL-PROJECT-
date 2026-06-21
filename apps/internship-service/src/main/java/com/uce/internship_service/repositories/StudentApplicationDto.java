package com.uce.internship_service.repositories;

import java.time.LocalDateTime;

public record StudentApplicationDto(
    String internshipId,
    String title,
    String company,
    Long postulacionId,
    String estado,
    String mensaje,
    LocalDateTime fechaPostulacion
) {}
