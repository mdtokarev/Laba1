package ui.viewmodel;

//Класс как копия прогонов для нашей таблицы, для колонок
public class RunRow {
    private final long id;
    private final long experimentId;
    private final String name;
    private final String operatorName;
    private final String createdAt;
    private final String updatedAt;

    public  RunRow(long id, long experimentId, String name, String operatorName, String createdAt, String updatedAt) {
        this.id = id;
        this.experimentId = experimentId;
        this.name = name;
        this.operatorName = operatorName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public long getId() {
        return id;
    }
    public long getExperimentId() {
        return experimentId;
    }
    public String getName() {
        return name;
    }
    public String getOperatorName() {
        return operatorName;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public String getUpdatedAt() {
        return updatedAt;
    }
}
