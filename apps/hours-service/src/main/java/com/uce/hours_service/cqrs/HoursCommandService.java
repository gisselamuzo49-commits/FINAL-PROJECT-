package com.uce.hours_service.cqrs;

import com.uce.hours_service.cqrs.commands.CreateHoursCommand;
import com.uce.hours_service.cqrs.commands.ValidateHoursCommand;
import com.uce.hours_service.models.EstadoHoras;
import com.uce.hours_service.models.OutboxEvent;
import com.uce.hours_service.models.RegistroHoras;
import com.uce.hours_service.repositories.OutboxEventRepository;
import com.uce.hours_service.repositories.RegistroHorasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class HoursCommandService {

    @Autowired
    private RegistroHorasRepository repository;

    @Autowired
    private OutboxEventRepository outboxRepository;

    @Transactional
    public RegistroHoras createHoursRegistration(CreateHoursCommand command) {
        RegistroHoras registro = new RegistroHoras();
        registro.setEstudianteId(command.getEstudianteId());
        registro.setProyectoId(command.getProyectoId());
        registro.setFecha(command.getFecha());
        registro.setHoras(command.getHoras());
        registro.setDescripcionActividad(command.getDescripcionActividad());
        registro.setEstado(EstadoHoras.PENDIENTE);

        RegistroHoras saved = repository.save(registro);
        
        OutboxEvent event = new OutboxEvent(
            saved.getEstudianteId(),
            "HORAS_REGISTRADAS",
            toJson(saved)
        );
        outboxRepository.save(event);

        return saved;
    }

    @Transactional
    public Optional<RegistroHoras> validarHoursRegistration(ValidateHoursCommand command) {
        return repository.findById(command.getId()).map(registro -> {
            registro.setTutorId(command.getTutorId());
            registro.setFechaValidacion(LocalDateTime.now());
            registro.setEstado(command.isAprobado() ? EstadoHoras.VALIDADO : EstadoHoras.RECHAZADO);
            RegistroHoras saved = repository.save(registro);
            
            OutboxEvent event = new OutboxEvent(
                saved.getEstudianteId(),
                "HORAS_REGISTRADAS",
                toJson(saved)
            );
            outboxRepository.save(event);

            return saved;
        });
    }

    private String toJson(RegistroHoras r) {
        String fechaValidacion = r.getFechaValidacion() != null
                ? "\"" + r.getFechaValidacion() + "\""
                : "null";
        String tutorId = r.getTutorId() != null
                ? "\"" + r.getTutorId() + "\""
                : "null";

        return "{"
                + "\"id\":" + r.getId() + ","
                + "\"estudianteId\":\"" + r.getEstudianteId() + "\","
                + "\"proyectoId\":\"" + r.getProyectoId() + "\","
                + "\"fecha\":\"" + r.getFecha() + "\","
                + "\"horas\":" + r.getHoras() + ","
                + "\"descripcionActividad\":\"" + escapeJson(r.getDescripcionActividad()) + "\","
                + "\"estado\":\"" + r.getEstado() + "\","
                + "\"tutorId\":" + tutorId + ","
                + "\"fechaValidacion\":" + fechaValidacion
                + "}";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

