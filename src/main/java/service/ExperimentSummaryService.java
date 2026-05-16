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

        //Создаем Map для статистики и именно EnumMap так как удобно работать Enum
        Map<MeasurementParam, ParamStatistics> statisticsByParam = new EnumMap<>(MeasurementParam.class);

        //Берем все прогоны эксперемента и проходимся по ним и для каждого прогона берем все его результаты
        for (Run run : runService.listByExpId(experimentId)) {
            for (RunResult result : resultService.listByRunId(run.getId())) {
                //Если стаистика по такому эксперементу уже есть просто добавляем, если нет создаем и добовляем
                ParamStatistics statistics = statisticsByParam.get(result.getParam());

                if (statistics == null) {
                    statistics = new ParamStatistics(result.getParam());
                    statisticsByParam.put(result.getParam(), statistics);
                }

                //Добовляем значение результатат в статистику
                statistics.addValue(result.getValue());
            }
        }

        //Когда все результаты обработаны берем все занчения из Map  и превращаем в список
        List<ParamStatistics> statistics = new ArrayList<>(statisticsByParam.values());
         //Создаем и возвращаем итоговое хранилище
        return new ExperimentSummary(experiment.getId(), experiment.getName(),statistics);

    }
}