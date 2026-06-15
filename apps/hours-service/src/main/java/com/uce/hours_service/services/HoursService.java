package com.uce.hours_service.services;

import com.uce.hours_service.models.EstadoHoras;
import com.uce.hours_service.models.RegistroHoras;
import com.uce.hours_service.repositories.RegistroHorasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class HoursService {

    @Autowired
    private RegistroHorasRepository repository;

    public RegistroHoras createHoursRegistration(RegistroHoras registro) {
        registro.setEstado(EstadoHoras.PENDIENTE);
        return repository.save(registro);
    }

    public Optional<RegistroHoras> validarHoursRegistration(Long id, String tutorId, boolean aprobado) {
        return repository.findById(id).map(registro -> {
            registro.setTutorId(tutorId);
            registro.setFechaValidacion(LocalDateTime.now());
            registro.setEstado(aprobado ? EstadoHoras.VALIDADO : EstadoHoras.RECHAZADO);
            return repository.save(registro);
        });
    }
}
