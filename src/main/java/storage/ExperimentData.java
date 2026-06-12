package storage;

//Удобное хранение эксперемнтов в удобном формате для JSON
public class ExperimentData {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private String createdAt;
    private String updatedAt;

    public ExperimentData() {
    }

    public ExperimentData(Long id, String name, String description, Long ownerId, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Long getOwnerId() {return ownerId; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setOwnerId(Long ownerId) {this.ownerId = ownerId;}
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}

