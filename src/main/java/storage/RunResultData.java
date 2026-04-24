package storage;

//Удобное хранение результатов прогонов в удобном формате для JSON
public class RunResultData {
    private Long id;
    private Long runId;
    private String param;
    private Double value;
    private String unit;
    private String comment;
    private String createdAt;
    private String updatedAt;

    public RunResultData() {
    }

    public RunResultData(Long id, Long runId, String param, Double value,
                         String unit, String comment, String createdAt, String updatedAt) {
        this.id = id;
        this.runId = runId;
        this.param = param;
        this.value = value;
        this.unit = unit;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getRunId() { return runId; }
    public String getParam() { return param; }
    public Double getValue() { return value; }
    public String getUnit() { return unit; }
    public String getComment() { return comment; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setRunId(Long runId) { this.runId = runId; }
    public void setParam(String param) { this.param = param; }
    public void setValue(Double value) { this.value = value; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setComment(String comment) { this.comment = comment; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
