package service;

import domain.MeasurementParam;

public class ParamStatistics {
    public final MeasurementParam param;
    private int count;
    private double min;
    private double max;
    private double sum;

    public ParamStatistics(MeasurementParam param) {
        this.param = param;
        this.count = 0;
        this.min = Double.POSITIVE_INFINITY;//Первоночально ставим минимум как очень большое значение
        this.max = Double.NEGATIVE_INFINITY;//Первоночально ставим максимум как очень малое значение
        this.sum = 0;
    }

    //Метод добавляющий новое значение в статистику
    public void addValue(double value) {
        count++;//+1
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
}

