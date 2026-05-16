package service;

import domain.Experiment;
import domain.MeasurementParam;
import domain.Run;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExperimentSummaryServiceTest {
    private ExperimentService experimentService;
    private RunService runService;
    private RunResultService resultService;
    private ExperimentSummaryService summaryService;

    @BeforeEach
    void setUp() {
        experimentService = new ExperimentService();
        runService = new RunService(experimentService);
        resultService = new RunResultService(runService);
        summaryService = new ExperimentSummaryService(experimentService, runService, resultService);
    }

    @Test
//    проверяем, что статистика группируется по параметру и правильно считает значения
    void buildForExperimentCalculatesStatisticsGroupedByParam() {
        Experiment experiment = experimentService.add("Experiment", "Description", "owner");
        Run firstRun = runService.add(experiment.getId(), "Run 1", "operator");
        Run secondRun = runService.add(experiment.getId(), "Run 2", "operator");
        resultService.add(firstRun.getId(), MeasurementParam.pH, 6.0, "pH", "low");
        resultService.add(firstRun.getId(), MeasurementParam.pH, 8.0, "pH", "high");
        resultService.add(secondRun.getId(), MeasurementParam.Temperature, 20.0, "C", "first");
        resultService.add(secondRun.getId(), MeasurementParam.Temperature, 24.0, "C", "second");

        ExperimentSummary summary = summaryService.buildForExperiment(experiment.getId());
        Map<MeasurementParam, ParamStatistics> statisticsByParam = summary.getStatistics().stream()
                .collect(Collectors.toMap(ParamStatistics::getParam, statistics -> statistics));

//        првоеряем соответствие всех данных
        assertEquals(experiment.getId(), summary.getExperimentId());
        assertEquals("Experiment", summary.getExperimentName());
        assertEquals(2, statisticsByParam.size());
        assertStatistics(statisticsByParam.get(MeasurementParam.pH), 2, 6.0, 8.0, 7.0);
        assertStatistics(statisticsByParam.get(MeasurementParam.Temperature), 2, 20.0, 24.0, 22.0);
    }

    @Test
//    проверяем случай когда прогон есть а результатов нет
    void buildForExperimentReturnsEmptyStatisticsWhenNoResultsExist() {
        Experiment experiment = experimentService.add("Experiment", "Description", "owner");
        runService.add(experiment.getId(), "Run", "operator");

        ExperimentSummary summary = summaryService.buildForExperiment(experiment.getId());

        assertEquals(experiment.getId(), summary.getExperimentId());
        assertEquals("Experiment", summary.getExperimentName());
//        проверяем что статистика пустая
        assertTrue(summary.getStatistics().isEmpty());
    }

    //    вспомогательный метод который првоеряет кол-во, макс, мин и сред. чтобы каждый раз не прописывать
    private void assertStatistics(ParamStatistics statistics, int count, double min, double max, double average) {
        assertEquals(count, statistics.getCount());
        assertEquals(min, statistics.getMin());
        assertEquals(max, statistics.getMax());
        assertEquals(average, statistics.getAverage());
    }
}