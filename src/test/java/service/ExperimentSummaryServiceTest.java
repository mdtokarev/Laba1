package service;

import domain.MeasurementParam;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExperimentSummaryServiceTest {

    @Test
    //Проверка коректности статистики
    void shouldBuildSummaryForExperiment() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var summaryService = new ExperimentSummaryService(experimentService, runService, resultService);

        var experiment = experimentService.add("Exp", "desc", 1);
        var run = runService.add(experiment.getId(), "Run", "operator");

        resultService.add(run.getId(), MeasurementParam.pH, 6.0, "pH", null);
        resultService.add(run.getId(), MeasurementParam.pH, 8.0, "pH", null);
        resultService.add(run.getId(), MeasurementParam.Temperature, 20.0, "C", null);

        ExperimentSummary summary = summaryService.buildForExperiment(experiment.getId());

        assertEquals(experiment.getId(), summary.getExperimentId());
        assertEquals("Exp", summary.getExperimentName());
        assertEquals(2, summary.getStatistics().size());

        ParamStatistics phStatistics = summary.getStatistics()
                .stream()
                .filter(statistics -> statistics.getParam() == MeasurementParam.pH)
                .findFirst()
                .orElseThrow();

        assertEquals(2, phStatistics.getCount());
        assertEquals(6.0, phStatistics.getMin());
        assertEquals(8.0, phStatistics.getMax());
        assertEquals(7.0, phStatistics.getAverage());
    }

    @Test
    //Проверка ситуации когда эксперемнт есть но результатоа нет
    void shouldReturnEmptyStatisticsWhenExperimentHasNoResults() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var summaryService = new ExperimentSummaryService(experimentService, runService, resultService);

        var experiment = experimentService.add("Empty exp", "desc", 1);

        ExperimentSummary summary = summaryService.buildForExperiment(experiment.getId());

        assertEquals("Empty exp", summary.getExperimentName());
        assertTrue(summary.getStatistics().isEmpty());
    }
}
