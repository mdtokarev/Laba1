package ui.dialog;

//Клаас контейнер для  данных эксперемнтов из пользовательского окна
public class ExperimentFormData {
    private final String name;
    private final String description;

    public ExperimentFormData(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
