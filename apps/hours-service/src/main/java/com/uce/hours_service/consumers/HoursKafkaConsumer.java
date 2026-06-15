package com.uce.hours_service.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uce.hours_service.models.HistorialEntry;
import com.uce.hours_service.models.HorasResumen;
import com.uce.hours_service.repositories.HorasResumenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class HoursKafkaConsumer {

    @Autowired
    private HorasResumenRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "horas.registradas", groupId = "hours-service-group")
    public void consume(String message) {
        try {
            HorasRegistradasEvent event = objectMapper.readValue(message, HorasRegistradasEvent.class);
            if (event.getEstudianteId() == null) {
                return;
            }

            HorasResumen resumen = repository.findById(event.getEstudianteId())
                    .orElseGet(() -> new HorasResumen(event.getEstudianteId()));

            // Find existing entry in history
            boolean updated = false;
            if (resumen.getHistorial() == null) {
                resumen.setHistorial(new ArrayList<>());
            }
            for (HistorialEntry entry : resumen.getHistorial()) {
                if (entry.getRegistroId() != null && entry.getRegistroId().equals(event.getId())) {
                    entry.setProyectoId(event.getProyectoId());
                    entry.setFecha(event.getFecha());
                    entry.setHoras(event.getHoras());
                    entry.setEstado(event.getEstado());
                    entry.setDescripcionActividad(event.getDescripcionActividad());
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                resumen.getHistorial().add(new HistorialEntry(
                        event.getId(),
                        event.getProyectoId(),
                        event.getFecha(),
                        event.getHoras(),
                        event.getEstado(),
                        event.getDescripcionActividad()
                ));
            }

            // Recalculate totals
            double validadas = 0.0;
            double pendientes = 0.0;
            for (HistorialEntry entry : resumen.getHistorial()) {
                if ("VALIDADO".equalsIgnoreCase(entry.getEstado())) {
                    validadas += entry.getHoras();
                } else if ("PENDIENTE".equalsIgnoreCase(entry.getEstado())) {
                    pendientes += entry.getHoras();
                }
            }
            resumen.setTotalHorasValidadas(validadas);
            resumen.setTotalHorasPendientes(pendientes);

            repository.save(resumen);

        } catch (Exception e) {
            System.err.println("Error processing Kafka event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static class HorasRegistradasEvent {
        private String id;
        private String estudianteId;
        private String proyectoId;
        private String fecha;
        private Double horas;
        private String descripcionActividad;
        private String estado;
        private String tutorId;
        private String fechaValidacion;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getEstudianteId() { return estudianteId; }
        public void setEstudianteId(String estudianteId) { this.estudianteId = estudianteId; }

        public String getProyectoId() { return proyectoId; }
        public void setProyectoId(String proyectoId) { this.proyectoId = proyectoId; }

        public String getFecha() { return fecha; }
        public void setFecha(String fecha) { this.fecha = fecha; }

        public Double getHoras() { return horas; }
        public void setHoras(Double horas) { this.horas = horas; }

        public String getDescripcionActividad() { return descripcionActividad; }
        public void setDescripcionActividad(String descripcionActividad) { this.descripcionActividad = descripcionActividad; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public String getTutorId() { return tutorId; }
        public void setTutorId(String tutorId) { this.tutorId = tutorId; }

        public String getFechaValidacion() { return fechaValidacion; }
        public void setFechaValidacion(String fechaValidacion) { this.fechaValidacion = fechaValidacion; }
    }
}
