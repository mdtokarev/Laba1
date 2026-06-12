package service;

import domain.Experiment;
import domain.MeasurementParam;
import domain.Run;
import domain.RunResult;

import java.util.*;

//Создаем сервис который будет считать статистику по эксперементу
public class ExperimentSummaryService {
    private final ExperimentService experimentService;
    private final RunService runService;
    private final RunResultService resultService;

    public ExperimentSummaryService(ExperimentService experimentService, RunService runService, RunResultService resultService) {
        this.experimentService = experimentService;
        this.runService = runService;
        this.resultService = resultService;
    }

    //Строим хранилище по эксперементу
    public ExperimentSummary buildForExperiment(long experimentId) {
        //Получаем каждый эксперемнт, если его нет ошибка
        Experiment experiment = experimentService.getById(experimentId);
        Map<SummaryKey, ParamStatistics> statisticsByKey = new LinkedHashMap<>(); // группировка по паре param+unit

        //Берем все прогоны эксперемента и проходимся по ним и для каждого прогона берем все его результаты
        for (Run run : runService.listByExpId(experimentId)) {
            for (RunResult result : resultService.listByRunId(run.getId())) {
                String unit = normalizeUnit(result.getUnit());
                SummaryKey key = new SummaryKey(result.getParam(), unit);
                //Если стаистика по такому эксперементу уже есть просто добавляем, если нет создаем и добовляем
                ParamStatistics statistics = statisticsByKey.get(key); // поиск статы по ключу param+unit

                if (statistics == null) {
                    statistics = new ParamStatistics(result.getParam(), unit);
                    statisticsByKey.put(key, statistics);
                }

                //Добовляем значение результатат в статистику
                statistics.addValue(result.getValue());
            }
        }

        // список итоговой статы
        List<ParamStatistics> statistics = new ArrayList<>(statisticsByKey.values());
         //Создаем и возвращаем итоговое хранилище
        return new ExperimentSummary(experiment.getId(), experiment.getName(),statistics);
    }

    private record SummaryKey(MeasurementParam param, String unit) {}

    private String normalizeUnit(String unit) {
        if (unit == null) {
            return "";
        }

        String normalized = unit.trim()
                .replace("В°", "°")
                .replace("Â°", "°")
                .replace('К', 'K')
                .replace('к', 'k')
                .replace('С', 'C')
                .replace('с', 'c');

        if (normalized.equalsIgnoreCase("pH")) {
            return "pH";
        }
        if (normalized.equalsIgnoreCase("K")) {
            return "K";
        }
        if (normalized.equalsIgnoreCase("C")) {
            return "C";
        }
        if (normalized.equalsIgnoreCase("°C")) {
            return "°C";
        }

        return normalized;
    }
}