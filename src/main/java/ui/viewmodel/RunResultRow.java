package ui.viewmodel;

//Класс как копия результатов для нашей таблицы, для колонок
public class RunResultRow {
    private final long id;
    private final long runId;
    private final String param;
    private final double value;
    private final String unit;
    private final String comment;
    private final String createdAt;
    private final String updatedAt;

    public RunResultRow(long id, long runId, String param, double value, String unit, String comment, String createdAt, String updatedAt) {
        this.id = id;
        this.runId = runId;
        this.param = param;
        this.value = value;
        this.unit = unit;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }
    public long getRunId() {
        return runId;
    }
    public String getParam() {
        return param;
    }
    public double getValue() {
        return value;
    }
    public String getUnit() {
        return unit;
    }
    public String getComment() {
        return comment;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public String getUpdatedAt() {
        return updatedAt;
    }
}
