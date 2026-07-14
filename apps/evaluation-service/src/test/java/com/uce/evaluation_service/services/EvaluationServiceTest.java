package com.uce.evaluation_service.services;

import com.uce.evaluation_service.client.StudentInfo;
import com.uce.evaluation_service.client.UserServiceClient;
import com.uce.evaluation_service.dto.EvaluacionConEstudiante;
import com.uce.evaluation_service.models.EvaluacionFinal;
import com.uce.evaluation_service.repositories.EvaluacionFinalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    private EvaluacionFinalRepository repository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private EvaluationService evaluationService;

    @Test
    void createEvaluationSavesAndReturnsEvaluation() {
        EvaluacionFinal evaluation = evaluation();
        when(repository.save(evaluation)).thenReturn(evaluation);

        EvaluacionFinal result = evaluationService.createEvaluation(evaluation);

        assertSame(evaluation, result);
        verify(repository).save(evaluation);
    }

    @Test
    void getEvaluationByIdReturnsRepositoryResult() {
        EvaluacionFinal evaluation = evaluation();
        when(repository.findById(7L)).thenReturn(Optional.of(evaluation));

        Optional<EvaluacionFinal> result = evaluationService.getEvaluationById(7L);

        assertEquals(Optional.of(evaluation), result);
        verify(repository).findById(7L);
    }

    @Test
    void getEvaluationsByStudentIdEnrichesEveryEvaluationWithOneGrpcCall() {
        EvaluacionFinal first = evaluation();
        EvaluacionFinal second = evaluation();
        second.setId(8L);
        when(repository.findByEstudianteId("student-1")).thenReturn(List.of(first, second));
        when(userServiceClient.getStudentInfo("student-1"))
                .thenReturn(Optional.of(new StudentInfo(" Ana ", " López ", "Software")));

        List<EvaluacionConEstudiante> result =
                evaluationService.getEvaluationsByStudentId("student-1");

        assertEquals(2, result.size());
        assertEvaluation(first, result.get(0), "Ana López", "Software");
        assertEvaluation(second, result.get(1), "Ana López", "Software");
        verify(repository).findByEstudianteId("student-1");
        verify(userServiceClient).getStudentInfo("student-1");
    }

    @ParameterizedTest
    @MethodSource("studentNames")
    void getEvaluationsByStudentIdBuildsAvailableStudentName(
            String firstName,
            String lastName,
            String expectedName) {
        EvaluacionFinal evaluation = evaluation();
        when(repository.findByEstudianteId("student-1")).thenReturn(List.of(evaluation));
        when(userServiceClient.getStudentInfo("student-1"))
                .thenReturn(Optional.of(new StudentInfo(firstName, lastName, "Engineering")));

        List<EvaluacionConEstudiante> result =
                evaluationService.getEvaluationsByStudentId("student-1");

        assertEquals(expectedName, result.get(0).getNombre());
        assertEquals("Engineering", result.get(0).getCarrera());
    }

    @Test
    void getEvaluationsByStudentIdKeepsStudentFieldsNullWhenGrpcHasNoResult() {
        EvaluacionFinal evaluation = evaluation();
        when(repository.findByEstudianteId("student-1")).thenReturn(List.of(evaluation));
        when(userServiceClient.getStudentInfo("student-1")).thenReturn(Optional.empty());

        List<EvaluacionConEstudiante> result =
                evaluationService.getEvaluationsByStudentId("student-1");

        assertNull(result.get(0).getNombre());
        assertNull(result.get(0).getCarrera());
    }

    private static Stream<Arguments> studentNames() {
        return Stream.of(
                Arguments.of(" Ana ", null, "Ana"),
                Arguments.of(null, " López ", "López"),
                Arguments.of(" Ana ", " ", "Ana"),
                Arguments.of(" ", " ", ""));
    }

    private static EvaluacionFinal evaluation() {
        EvaluacionFinal evaluation = new EvaluacionFinal();
        evaluation.setId(7L);
        evaluation.setEstudianteId("student-1");
        evaluation.setProyectoId("project-1");
        evaluation.setTutorId("tutor-1");
        evaluation.setFechaEvaluacion(LocalDate.of(2026, 7, 1));
        evaluation.setCalificacion(new BigDecimal("9.25"));
        evaluation.setComentarios("Excellent");
        evaluation.setCreatedAt(LocalDateTime.of(2026, 7, 1, 10, 30));
        return evaluation;
    }

    private static void assertEvaluation(
            EvaluacionFinal evaluation,
            EvaluacionConEstudiante result,
            String expectedName,
            String expectedCareer) {
        assertEquals(evaluation.getId(), result.getId());
        assertEquals(evaluation.getEstudianteId(), result.getEstudianteId());
        assertEquals(evaluation.getProyectoId(), result.getProyectoId());
        assertEquals(evaluation.getTutorId(), result.getTutorId());
        assertEquals(evaluation.getFechaEvaluacion(), result.getFechaEvaluacion());
        assertEquals(evaluation.getCalificacion(), result.getCalificacion());
        assertEquals(evaluation.getComentarios(), result.getComentarios());
        assertEquals(evaluation.getCreatedAt(), result.getCreatedAt());
        assertEquals(expectedName, result.getNombre());
        assertEquals(expectedCareer, result.getCarrera());
    }
}
