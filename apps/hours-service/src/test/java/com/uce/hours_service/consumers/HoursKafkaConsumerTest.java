package com.uce.hours_service.consumers;

import com.uce.hours_service.models.HistorialEntry;
import com.uce.hours_service.models.HorasResumen;
import com.uce.hours_service.repositories.HorasResumenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HoursKafkaConsumerTest {

    @Mock
    private HorasResumenRepository repository;

    @InjectMocks
    private HoursKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void consume_NewStudent_CreatesNewHorasResumenAndCalculatesTotals() {
        String studentId = "estudiante-1";
        String message = "{"
                + "\"id\":\"1\","
                + "\"estudianteId\":\"" + studentId + "\","
                + "\"proyectoId\":\"proj-10\","
                + "\"fecha\":\"2026-06-10\","
                + "\"horas\":4.5,"
                + "\"descripcionActividad\":\"Testing act\","
                + "\"estado\":\"PENDIENTE\","
                + "\"tutorId\":null,"
                + "\"fechaValidacion\":null"
                + "}";

        when(repository.findById(studentId)).thenReturn(Optional.empty());

        consumer.consume(message);

        ArgumentCaptor<HorasResumen> captor = ArgumentCaptor.forClass(HorasResumen.class);
        verify(repository, times(1)).save(captor.capture());

        HorasResumen saved = captor.getValue();
        assertEquals(studentId, saved.getEstudianteId());
        assertNull(saved.getNombre());
        assertNull(saved.getCarrera());
        assertEquals(0.0, saved.getTotalHorasValidadas());
        assertEquals(4.5, saved.getTotalHorasPendientes());
        assertEquals(1, saved.getHistorial().size());

        HistorialEntry entry = saved.getHistorial().get(0);
        assertEquals("1", entry.getRegistroId());
        assertEquals("proj-10", entry.getProyectoId());
        assertEquals("2026-06-10", entry.getFecha());
        assertEquals(4.5, entry.getHoras());
        assertEquals("PENDIENTE", entry.getEstado());
        assertEquals("Testing act", entry.getDescripcionActividad());
    }

    @Test
    void consume_ExistingStudent_UpdatesExistingEntryAndRecalculatesTotals() {
        String studentId = "estudiante-1";

        HorasResumen existing = new HorasResumen(studentId);
        existing.setHistorial(new ArrayList<>());
        existing.getHistorial().add(new HistorialEntry(
                "1", "proj-10", "2026-06-10", 4.5, "PENDIENTE", "Testing act"
        ));
        existing.setTotalHorasPendientes(4.5);
        existing.setTotalHorasValidadas(0.0);

        String message = "{"
                + "\"id\":\"1\","
                + "\"estudianteId\":\"" + studentId + "\","
                + "\"proyectoId\":\"proj-10\","
                + "\"fecha\":\"2026-06-10\","
                + "\"horas\":4.5,"
                + "\"descripcionActividad\":\"Testing act updated\","
                + "\"estado\":\"VALIDADO\","
                + "\"tutorId\":\"tutor-5\","
                + "\"fechaValidacion\":\"2026-06-14T19:30:00\""
                + "}";

        when(repository.findById(studentId)).thenReturn(Optional.of(existing));

        consumer.consume(message);

        ArgumentCaptor<HorasResumen> captor = ArgumentCaptor.forClass(HorasResumen.class);
        verify(repository, times(1)).save(captor.capture());

        HorasResumen saved = captor.getValue();
        assertEquals(studentId, saved.getEstudianteId());
        assertEquals(4.5, saved.getTotalHorasValidadas());
        assertEquals(0.0, saved.getTotalHorasPendientes());
        assertEquals(1, saved.getHistorial().size());

        HistorialEntry entry = saved.getHistorial().get(0);
        assertEquals("1", entry.getRegistroId());
        assertEquals("VALIDADO", entry.getEstado());
        assertEquals("Testing act updated", entry.getDescripcionActividad());
    }
}
