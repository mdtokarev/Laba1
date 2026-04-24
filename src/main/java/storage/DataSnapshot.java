package storage;

import java.util.List;

//Создаем хранилище всех списков для дальнейшего сохранения и загрузки
public class DataSnapshot {
    private List<ExperimentData> experiments;
    private List<RunData> runs;
    private List<RunResultData> runResults;

    public DataSnapshot() {
    }

    public DataSnapshot(List<ExperimentData> experiments, List<RunData> runs, List<RunResultData> runResults) {
        this.experiments = experiments;
        this.runs = runs;
        this.runResults = runResults;
    }

    public List<ExperimentData> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<ExperimentData> experiments) {
        this.experiments = experiments;
    }

    public List<RunData> getRuns() {
        return runs;
    }

    public void setRuns(List<RunData> runs) {
        this.runs = runs;
    }

    public List<RunResultData> getRunResults() {
        return runResults;
    }

    public void setRunResults(List<RunResultData> runResults) {
        this.runResults = runResults;
    }
}
