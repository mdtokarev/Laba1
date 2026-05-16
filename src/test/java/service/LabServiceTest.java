package service;

import domain.Experiment;
import domain.MeasurementParam;
import domain.Run;
import domain.RunResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import validation.ValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LabServiceTest {
    private ExperimentService experimentService;
    private RunService runService;
    private RunResultService resultService;
    private LabService labService;

    // BeforeEach - описываем то что нужно выполнить перед каждым тестом
    @BeforeEach
    void setUp() {
        experimentService = new ExperimentService();
        runService = new RunService(experimentService);
        resultService = new RunResultService(runService);
//        создаем лабсервис который умеет управлять всеми одновременно
        labService = new LabService(experimentService, runService, resultService);
    }

    @Test
//    если через лабсервис удаляем эксперимент, то удалятся и все дети
    void removeExperimentWithChildrenRemovesRunsAndResults() {
        Experiment experiment = experimentService.add("Experiment", "Description", "owner");
        Run firstRun = runService.add(experiment.getId(), "Run 1", "operator");
        Run secondRun = runService.add(experiment.getId(), "Run 2", "operator");
        RunResult firstResult = resultService.add(firstRun.getId(), MeasurementParam.pH, 7.0, "pH", "ok");
        RunResult secondResult = resultService.add(secondRun.getId(), MeasurementParam.Temperature, 22.5, "C", "ok");

        labService.removeExperimentWithChildren(experiment.getId());
//        после удаления проверяем что коллекции пустые
        assertEquals(0, experimentService.list().size());
        assertEquals(0, runService.list().size());
        assertEquals(0, resultService.list().size());

//        проверяем что родителя и детей нельзя больше получить по айди
        assertThrows(ValidationException.class, () -> experimentService.getById(experiment.getId()));
        assertThrows(ValidationException.class, () -> runService.getById(firstRun.getId()));
        assertThrows(ValidationException.class, () -> runService.getById(secondRun.getId()));
        assertThrows(ValidationException.class, () -> resultService.getById(firstResult.getId()));
        assertThrows(ValidationException.class, () -> resultService.getById(secondResult.getId()));
    }

    @Test
//    удаление результатов и прогона у только выбранного прогона
    void removeRunWithResultsRemovesOnlySelectedRunAndItsResults() {
        Experiment experiment = experimentService.add("Experiment", "Description", "owner");
        Run removedRun = runService.add(experiment.getId(), "Run 1", "operator");
        Run remainingRun = runService.add(experiment.getId(), "Run 2", "operator");
        RunResult removedResult = resultService.add(removedRun.getId(), MeasurementParam.pH, 7.0, "pH", "ok");
        RunResult remainingResult = resultService.add(remainingRun.getId(), MeasurementParam.Concentration, 4.0, "mg/L", "ok");

        //        удаляем только первый прогон с его результатами
        labService.removeRunWithResults(removedRun.getId());

//        проверяем что остался эксперимент и прогон, который не трогали
        assertEquals(1, experimentService.list().size());
        assertEquals(1, runService.list().size());
        assertEquals(1, resultService.list().size());

//        проверяем что действительно удалилось первое
        assertThrows(ValidationException.class, () -> runService.getById(removedRun.getId()));
        assertThrows(ValidationException.class, () -> resultService.getById(removedResult.getId()));
        assertEquals(remainingRun.getId(), runService.getById(remainingRun.getId()).getId());
        assertEquals(remainingResult.getId(), resultService.getById(remainingResult.getId()).getId());
    }
}
