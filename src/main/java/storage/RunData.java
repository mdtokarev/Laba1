package storage;

//Удобное хранение прогонов в удобном формате для JSON
public class RunData {
    private Long id;
    private Long experimentId;
    private String name;
    private String operatorName;
    private String createdAt;
    private String updatedAt;

    public RunData() {
    }

    public RunData(Long id, Long experimentId, String name,
                   String operatorName, String createdAt, String updatedAt) {
        this.id = id;
        this.experimentId = experimentId;
        this.name = name;
        this.operatorName = operatorName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getExperimentId() { return experimentId; }
    public String getName() { return name; }
    public String getOperatorName() { return operatorName; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setExperimentId(Long experimentId) { this.experimentId = experimentId; }
    public void setName(String name) { this.name = name; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}

