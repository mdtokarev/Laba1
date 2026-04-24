package storage;

import domain.Experiment;
import domain.MeasurementParam;
import domain.Run;
import domain.RunResult;

import java.time.Instant;
import java.util.List;

// Переводит доменные объекты в простые JSON-объекты и обратно.
public class SnapshotMapper {

    //Метод для сохранения
    public DataSnapshot toSnapshot(List<Experiment> experiments, List<Run> runs, List<RunResult> results) {
        return new DataSnapshot(experiments.stream().map(this::toExperimentData).toList(), runs.stream().map(this::toRunData).toList(), results.stream().map(this::toRunResultData).toList());//Берем domain и превращаем их в domainData
    }

    //Методы для загрузки
    public List<Experiment> toExperiments(DataSnapshot snapshot) {
        return snapshot.getExperiments().stream().map(this::toExperiment).toList();//Берем ExperimentData и превращаем оратно в Experiment
    }

    public List<Run> toRuns(DataSnapshot snapshot) {
        return snapshot.getRuns().stream().map(this::toRun).toList();//Берем RunData и превращаем оратно в Run
    }

    public List<RunResult> toRunResults(DataSnapshot snapshot) {
        return snapshot.getRunResults().stream().map(this::toRunResult).toList();//Берем RunResultsData и превращаем оратно в RunResults
    }

    //Переводим один Experiment в один ExperimentData, все данные копируются, время превращаются в строки через toString
    private ExperimentData toExperimentData(Experiment experiment) {
        return new ExperimentData(experiment.getId(), experiment.getName(), experiment.getDescription(), experiment.getOwnerUsername(), experiment.getCreatedAt().toString(), experiment.getUpdatedAt().toString());
    }

    //Переводим один Run в один RunData, все данные копируются, время превращаются в строки через toString
    private RunData toRunData(Run run) {
        return new RunData(run.getId(), run.getExperimentId(), run.getName(), run.getOperatorName(), run.getCreatedAt().toString(), run.getUpdatedAt().toString());
    }

    //Переводим один RunResult в один RunResultData, все данные копируются, время превращаются в строки через toString
    private RunResultData toRunResultData(RunResult result) {
        return new RunResultData(result.getId(), result.getRunId(), result.getParam().name(), result.getValue(), result.getUnit(), result.getComment(), result.getCreatedAt().toString(), result.getUpdatedAt().toString());
    }

    //Переводим ExperimentData обратно в Experiment, копируя все даннные, время через Instant.parse
    private Experiment toExperiment(ExperimentData data) {
        return Experiment.restore(data.getId(), data.getName(), data.getDescription(), data.getOwnerUsername(), Instant.parse(data.getCreatedAt()), Instant.parse(data.getUpdatedAt()));//Используем именно Experiment.restore так как нам надо востановить объекты из файла
    }
//Переводим RunData обратно в Run, копируя все даннные, время через Instant.parse
    private Run toRun(RunData data) {
        return Run.restore(data.getId(), data.getExperimentId(), data.getName(), data.getOperatorName(), Instant.parse(data.getCreatedAt()), Instant.parse(data.getUpdatedAt()));//Используем именно Experiment.restore так как нам надо востановить объекты из файла
    }

    //Переводим RunResultData обратно в RunResult, копируя все даннные, время через Instant.parse
    private RunResult toRunResult(RunResultData data) {
        return RunResult.restore(data.getId(), data.getRunId(), MeasurementParam.valueOf(data.getParam()), data.getValue(), data.getUnit(), data.getComment(), Instant.parse(data.getCreatedAt()), Instant.parse(data.getUpdatedAt()));//Используем именно Experiment.restore так как нам надо востановить объекты из файла
    }
}

