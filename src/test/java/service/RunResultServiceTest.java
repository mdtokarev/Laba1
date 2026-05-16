package service;

import domain.MeasurementParam;
import org.junit.jupiter.api.Test;
import validation.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

public class RunResultServiceTest {

    @Test
//    Проверяем что результат добавляется только к существующему прогону
    void shouldAddResultForExistingRun() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);

        var experiment = experimentService.add("exp", "desc", "user");
        var run = runService.add(experiment.getId(), "run", "operator");
        var result = resultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", "comment");

        assertTrue(result.getId() > 0);
        assertEquals(run.getId(), result.getRunId());
        assertSame(result, resultService.getById(result.getId()));
    }

    @Test
//    Проверяем генерацию уникальных ID для каждого результата
    void shouldGenerateDifferentIdsForDifferentResults() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);

        var experiment = experimentService.add("exp", "desc", "user");
        var run = runService.add(experiment.getId(), "run", "operator");

        var first = resultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", "first");
        var second = resultService.add(run.getId(), MeasurementParam.Temperature, 20.0, "C", "second");

        assertTrue(first.getId() > 0);
        assertTrue(second.getId() > 0);
        assertTrue(first.getId() != second.getId());
    }

    @Test
//    Проверяем что результаты корректно добавляются в коллекцию и лист содержит их все
    void listShouldKeepAllResults() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);

        var experiment = experimentService.add("exp", "desc", "user");
        var run = runService.add(experiment.getId(), "run", "operator");
        var first = resultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", "first");
        var second = resultService.add(run.getId(), MeasurementParam.Temperature, 20.0, "C", "second");

        var results = resultService.list();

        assertEquals(2, results.size());
        assertTrue(results.contains(first));
        assertTrue(results.contains(second));
    }

    @Test
//    Проверяем что если запросить результаты конкретного прогона, то сервис вернёт именно их, а не все разом
    void listShouldReturnResultsByRunId() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);

        var experiment = experimentService.add("exp", "desc", "user");
        var firstRun = runService.add(experiment.getId(), "run1", "operator1");
        var secondRun = runService.add(experiment.getId(), "run2", "operator2");
        var expectedResult = resultService.add(firstRun.getId(), MeasurementParam.pH, 7.0, "pH", "first");
        resultService.add(secondRun.getId(), MeasurementParam.pH, 8.0, "pH", "second");

        var results = resultService.listByRunId(firstRun.getId());

        assertEquals(1, results.size());
        assertTrue(results.contains(expectedResult));
    }

    @Test
//    Проверяем что сервис корректно обновляет результат
    void shouldUpdateResult() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);

        var experiment = experimentService.add("exp", "desc", "user");
        var run = runService.add(experiment.getId(), "run", "operator");
        var result = resultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", "comment");

        var updated = resultService.update(result.getId(), MeasurementParam.Temperature, 25.0, "C", "updated");

        assertSame(result, updated);
        assertEquals(MeasurementParam.Temperature, updated.getParam());
        assertEquals(25.0, updated.getValue());
        assertEquals("C", updated.getUnit());
        assertEquals("updated", updated.getComment());
    }

    @Test
//    Проверяем что сервис корректно удаляет результат
    void shouldRemoveResult() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);

        var experiment = experimentService.add("exp", "desc", "user");
        var run = runService.add(experiment.getId(), "run", "operator");
        var result = resultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", "comment");

        resultService.remove(result.getId());

        assertEquals(0, resultService.list().size());
        assertThrows(ValidationException.class, () -> resultService.getById(result.getId()));
    }

    @Test
//    Проверяем валидацию добавления результата к несуществующему прогону
    void shouldThrowWhenAddingResultForNotExistingRun() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);

        assertThrows(ValidationException.class, () ->
                resultService.add(999L, MeasurementParam.pH, 7.0, "pH", "comment"));
    }

    @Test
//    Проверяем валидацию отсутствия результата в коллекции
    void shouldThrowWhenResultNotFound() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);

        assertThrows(ValidationException.class, () -> resultService.getById(999L));
    }
}
