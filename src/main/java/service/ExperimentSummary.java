package service;

import java.util.List;

//Создаем класс итоговой хранилище по эксперементу со всей статистикой
public class ExperimentSummary {
    private final long  experimentId;
    private final String experimentName;
    private final List<ParamStatistics> statistics;

    public ExperimentSummary(long experimentId, String experimentName, List<ParamStatistics> statistics) {
        this.experimentId = experimentId;
        this.experimentName = experimentName;
        this.statistics = statistics;
    }

    public long getExperimentId() {
        return experimentId;
    }
    public String getExperimentName() {
        return experimentName;
    }

    public List<ParamStatistics> getStatistics() {
        return statistics;
    }
}
