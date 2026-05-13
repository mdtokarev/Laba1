package ui.dialog;

//Клаас контейнер для  данных прогонов из пользовательского окна
public class RunFormData {
    private final String name;
    private final String operatorName;

    public  RunFormData(String name, String operatorName) {
        this.name = name;
        this.operatorName = operatorName;
    }

    public String getName() {
        return name;
    }

    public String getOperatorName() {
        return operatorName;
    }
}
