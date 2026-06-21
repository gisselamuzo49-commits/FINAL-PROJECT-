package com.uce.internship_service.repositories;

import java.time.LocalDateTime;

public record EstudianteAplicanteDto(
    String estudianteId,
    Long postulacionId,
    String estado,
    String mensaje,
    LocalDateTime fechaPostulacion
) {}
