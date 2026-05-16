package ui.viewmodel;

//Класс как копия статистики для нашей таблицы, для колонок
public class SummaryRow {
    private final String param;
    private final int count;
    private final double min;
    private final double max;
    private  final double average;

    public  SummaryRow(String param, int count, double min, double max, double average) {
        this.param = param;
        this.count = count;
        this.min = min;
        this.max = max;
        this.average = average;
    }

    public String getParam() {
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
    public double getAverage() {
        return average;
    }
}
