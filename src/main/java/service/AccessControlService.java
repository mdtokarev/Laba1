package service;

import domain.Experiment;
import domain.Run;
import domain.RunResult;
import validation.ValidationException;

//Класс для проверки прав доступа
public class AccessControlService {
    private final ExperimentService experimentService;
    private final RunService runService;
    private final RunResultService runResultService;

    public AccessControlService(ExperimentService experimentService, RunService runService, RunResultService runResultService) {
        this.experimentService = experimentService;
        this.runService = runService;
        this.runResultService = runResultService;
    }

    //Метод для проверки что пользователь может менять эксперимент
    public void checkCanModifyExperiment(long userId, long experimentId){
        //Находим эксперимент по id если эксперимента нет то ошибка
        Experiment experiment = experimentService.getById(experimentId);

        //Сравниваем id владельца и текущего пользователя, если не равны ошибка
        if (experiment.getOwnerId() != userId){
            throw new ValidationException("You don't have permission to modify this object");
        }
    }

    //Метод для проверки что пользователь может менять прогон
    public void checkCanModifyRun(long userId, long runId){
        //Находим прогон по id
        Run run = runService.getById(runId);
        //Проверяем по experimentId. Не напрямую а через эксперимент
        checkCanModifyExperiment(userId, run.getExperimentId());
    }

    //Метод для проверки что пользователь может менять результат прогон
    public void checkCanModifyResult(long userId, long resultId){
        //Находим результат по id
        RunResult runResult = runResultService.getById(resultId);
        //Проверяем по runId. Не напрямую а через прогон потом эксперимент
        checkCanModifyRun(userId, runResult.getRunId());
    }
}
