package service;

import domain.MeasurementParam;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LabServiceTest {

    @Test
    //Проверяем что при удалении эксперемнта удалиться все
    void shouldRemoveExperimentWithRunsAndResults() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var labService = new LabService(experimentService, runService, resultService);

        var experiment = experimentService.add("Exp", "desc", 1);
        var firstRun = runService.add(experiment.getId(), "Run 1", "operator");
        var secondRun = runService.add(experiment.getId(), "Run 2", "operator");

        resultService.add(firstRun.getId(), MeasurementParam.pH, 7.0, "pH", "ok");
        resultService.add(secondRun.getId(), MeasurementParam.Temperature, 22.0, "C", "ok");

        labService.removeExperimentWithChildren(experiment.getId());

        assertTrue(experimentService.list().isEmpty());
        assertTrue(runService.list().isEmpty());
        assertTrue(resultService.list().isEmpty());
    }

    @Test
    //Проверяет удаление прогона вместе с результатом
    void shouldRemoveRunWithResults() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var labService = new LabService(experimentService, runService, resultService);

        var experiment = experimentService.add("Exp", "desc", 1);
        var run = runService.add(experiment.getId(), "Run", "operator");

        resultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", "ok");
        resultService.add(run.getId(), MeasurementParam.Concentration, 2.5, "mg/L", null);

        labService.removeRunWithResults(run.getId());

        assertEquals(1, experimentService.list().size());
        assertTrue(runService.list().isEmpty());
        assertTrue(resultService.list().isEmpty());
    }
}
