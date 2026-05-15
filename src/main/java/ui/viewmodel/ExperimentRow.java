package ui.viewmodel;

//Класс как копия эксперементов для нашей таблицы, для колонок
public class ExperimentRow {
    private final long id;
    private final String name;
    private final String description;
    private final long ownerId;
    private final String createdAt;
    private final String updatedAt;

    public  ExperimentRow(long id, String name, String description, long ownerId, String createdAt, String updated) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.updatedAt = updated;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public long getOwnerId() {
        return  ownerId;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public String getUpdatedAt() {
        return updatedAt;
    }
}
