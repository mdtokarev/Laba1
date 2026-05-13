package ui.dialog;

import domain.MeasurementParam;

//Клаас контейнер для  данных результатов прогонов из пользовательского окна
public class RunResultFormData {
    private final MeasurementParam param;
    private final double value;
    private final String unit;
    private final String comment;

    public RunResultFormData(MeasurementParam param, double value, String unit, String comment) {
        this.param = param;
        this.value = value;
        this.unit = unit;
        this.comment = comment;
    }

    public MeasurementParam getParam() {
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

}
