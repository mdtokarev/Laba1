package service;

import domain.Run;
import domain.RunResult;

import java.util.ArrayList;
import java.util.List;

// Сервис для сложных операция родитель-ребенок
public class LabService {
    private final ExperimentService experimentService;
    private final RunService runService;
    private final RunResultService resultService;

    public LabService(ExperimentService experimentService, RunService runService, RunResultService resultService) {
        this.experimentService = experimentService;
        this.runService = runService;
        this.resultService = resultService;
    }

    // Удаляет эксперимент вместе со всеми его прогонами и результатами
    public void removeExperimentWithChildren(long experimentId) {
        // Берем копию списка прогонов, потому что дальше будем удалять данные
        List<Run> runs = new ArrayList<>(runService.listByExpId(experimentId));

        // Сначала удаляем все прогоны эксперимента вместе с их результатами
        for (Run run : runs) {
            removeRunWithResults(run.getId());
        }

        // После удаления зависимых данных можно удалить сам эксперимент
        experimentService.remove(experimentId);
    }

    // Удаляет прогон вместе со всеми его результатами
    public void removeRunWithResults(long runId) {
        // Берем копию списка результатов, потому что дальше будем удалять данные
        List<RunResult> results = new ArrayList<>(resultService.listByRunId(runId));

        // Сначала удаляем все результаты выбранного прогона
        for (RunResult result : results) {
            resultService.remove(result.getId());
        }

        // После удаления результатов можно удалить сам прогон
        runService.remove(runId);
    }
}