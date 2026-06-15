package com.uce.hours_service.services;

import com.uce.hours_service.models.EstadoHoras;
import com.uce.hours_service.models.RegistroHoras;
import com.uce.hours_service.repositories.RegistroHorasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class HoursService {

    static final String TOPIC = "horas.registradas";

    @Autowired
    private RegistroHorasRepository repository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    public RegistroHoras createHoursRegistration(RegistroHoras registro) {
        registro.setEstado(EstadoHoras.PENDIENTE);
        RegistroHoras saved = repository.save(registro);
        kafkaTemplate.send(TOPIC, saved.getEstudianteId(), toJson(saved));
        return saved;
    }

    public Optional<RegistroHoras> validarHoursRegistration(Long id, String tutorId, boolean aprobado) {
        return repository.findById(id).map(registro -> {
            registro.setTutorId(tutorId);
            registro.setFechaValidacion(LocalDateTime.now());
            registro.setEstado(aprobado ? EstadoHoras.VALIDADO : EstadoHoras.RECHAZADO);
            RegistroHoras saved = repository.save(registro);
            kafkaTemplate.send(TOPIC, saved.getEstudianteId(), toJson(saved));
            return saved;
        });
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Builds the Kafka payload JSON manually — no Jackson needed here since the
     * fields are simple strings/numbers and must match the schema in section 5
     * of 10-DISENO-HOURS-SERVICE.md exactly.
     */
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

    /** Minimal JSON-string escaping for the descripcionActividad free-text field. */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
