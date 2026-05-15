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
        //Берем domain и превращаем их в domainData
        return new DataSnapshot(experiments.stream().map(this::toExperimentData).toList(), runs.stream().map(this::toRunData).toList(), results.stream().map(this::toRunResultData).toList());
    }

    //Методы для загрузки
    public List<Experiment> toExperiments(DataSnapshot snapshot) {
        //Берем ExperimentData и превращаем оратно в Experiment
        return snapshot.getExperiments().stream().map(this::toExperiment).toList();
    }

    public List<Run> toRuns(DataSnapshot snapshot) {
        //Берем RunData и превращаем оратно в Run
        return snapshot.getRuns().stream().map(this::toRun).toList();
    }

    public List<RunResult> toRunResults(DataSnapshot snapshot) {
        //Берем RunResultsData и превращаем оратно в RunResults
        return snapshot.getRunResults().stream().map(this::toRunResult).toList();
    }

    //Переводим один Experiment в один ExperimentData, все данные копируются, время превращаются в строки через toString
    private ExperimentData toExperimentData(Experiment experiment) {
        return new ExperimentData(experiment.getId(), experiment.getName(), experiment.getDescription(), experiment.getOwnerId(), experiment.getCreatedAt().toString(), experiment.getUpdatedAt().toString());
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
        //Используем именно Experiment.restore так как нам надо востановить объекты из файла
        return Experiment.restore(data.getId(), data.getName(), data.getDescription(), data.getOwnerId(), Instant.parse(data.getCreatedAt()), Instant.parse(data.getUpdatedAt()));
    }
//Переводим RunData обратно в Run, копируя все даннные, время через Instant.parse
    private Run toRun(RunData data) {
        //Используем именно Run.restore так как нам надо востановить объекты из файла
        return Run.restore(data.getId(), data.getExperimentId(), data.getName(), data.getOperatorName(), Instant.parse(data.getCreatedAt()), Instant.parse(data.getUpdatedAt()));
    }

    //Переводим RunResultData обратно в RunResult, копируя все даннные, время через Instant.parse
    private RunResult toRunResult(RunResultData data) {
        //Используем именно RunResult.restore так как нам надо востановить объекты из файла
        return RunResult.restore(data.getId(), data.getRunId(), MeasurementParam.valueOf(data.getParam()), data.getValue(), data.getUnit(), data.getComment(), Instant.parse(data.getCreatedAt()), Instant.parse(data.getUpdatedAt()));
    }
}

