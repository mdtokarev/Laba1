package ui.dialog;

//Клаас контейнер для  данных эксперемнтов из пользовательского окна
public class ExperimentFormData {
    private final String name;
    private final String description;
    private final String ownerUsername;

    public  ExperimentFormData(String name, String description, String ownerUsername) {
        this.name = name;
        this.description = description;
        this.ownerUsername = ownerUsername;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }
}
