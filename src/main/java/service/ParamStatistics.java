package service;

import domain.MeasurementParam;

import java.util.ArrayList;
import java.util.List;

public class ParamStatistics {
    public final MeasurementParam param;
    private int count;
    private double min;
    private double max;
    private double sum;
    private final String unit;
    private final List<Double> values; // сюда будут складываться все значения конкретного параметра

    public ParamStatistics(MeasurementParam param) {
        this(param, ""); // если Е. И. не нужна или неизвестна, будет ""
    }
    public ParamStatistics(MeasurementParam param, String unit) {
        this.param = param;
        this.unit = unit;
        this.count = 0;
        this.min = Double.POSITIVE_INFINITY; //Первоночально ставим минимум как очень большое значение
        this.max = Double.NEGATIVE_INFINITY; //Первоночально ставим максимум как очень малое значение
        this.sum = 0;
        this.values = new ArrayList<>();
    }

    //Метод добавляющий новое значение в статистику
    public void addValue(double value) {
        count++;//+1
        values.add(value); // каждое значение теперь будет сохраняться для графика
        sum += value;//Добавляем значение к сумме
        min = Math.min(min, value);//Обновляем максимум и минимум
        max = Math.max(max, value);
    }

    public MeasurementParam getParam() {
        return param;
    }

    public int getCount() {
        return count;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    //Метод считающий среднее
    public double getAverage() {
        //Если измерений нет то средннее 0, если есть честно считаем
        if (count == 0) {
            return 0;
        }
        return sum / count;
    }

    // возвращаем копию списка, чтобы внешне нельзя было поменять внутреннее состояние paramstatistic
    public List<Double> getValues() {
        return new ArrayList<>(values);
    }

    public String getUnit() {
        return unit;
    }
}

