package ui.dialog;

import domain.MeasurementParam;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import service.ExperimentSummary;
import service.ParamStatistics;
import java.util.*;

public class SummaryDialog {
    private static final int MAX_BIN_COUNT = 8; // задаем максимум столбцов на гистограмме - 8

    // главный метод - создает и показывает dialog заданного размера, на вход получает готовую статистику
    public void show(ExperimentSummary summary) {
        Dialog<Void> dialog = new Dialog<>(); // просто окно для просмотра
        dialog.setTitle("Summary");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setContent(createContent(summary)); // кладем внутрь окна интерфейс со статистикой и гистограммой
        dialog.getDialogPane().setPrefSize(820, 560);
        dialog.showAndWait();
    }

    // метод собирает содержимое окна - заголовок, выбор Param, текстовая статистика и график
    private BorderPane createContent(ExperimentSummary summary) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12)); // внутренний отступ для содержимого

        Label title = new Label("Experiment: " + summary.getExperimentName());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        root.setTop(title);

        if (summary.getStatistics().isEmpty()) {
            // если результатов нет, то не строим пустой график
            root.setCenter(new Label("No results"));
            return root;
        }

        // выпадающий список параметров, достаем все, которые есть в статистике
        ComboBox<MeasurementParam> paramBox = new ComboBox<>(
                FXCollections.observableArrayList(getAvailableParams(summary))
        );
        ComboBox<String> unitBox = new ComboBox<>(); // выпадающий список юнитов
        paramBox.setPrefWidth(160);
        unitBox.setPrefWidth(160);

        VBox statisticsBox = createStatisticsBox(); // для count, min, max, avg
        BarChart<String, Number> chart = createChart(); // график

        // слушатель на изменение выбранного параметра - обновление статистики и графика
        paramBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            List<String> units = getAvailableUnits(summary, newValue); // для выбранного параметра ищем доступные ЕИ
            unitBox.setItems(FXCollections.observableArrayList(units));
            unitBox.setDisable(units.isEmpty());

            String selectedUnit = null;
            if (units.isEmpty()) {
                unitBox.setValue(selectedUnit); // если ЕИ нет - ставим null
            } else {
                selectedUnit = units.get(0); // если ЕИ есть - выбираем первую
                unitBox.setValue(selectedUnit);
            }
            updateSelection(summary, statisticsBox, chart, newValue, selectedUnit);
        });

        unitBox.valueProperty().addListener((observable, oldValue, newValue) ->
                updateSelection(summary, statisticsBox, chart, paramBox.getValue(), newValue)
        );
        paramBox.setValue(paramBox.getItems().get(0)); // при открытии окна сразу выбран первый параметр из списка

        HBox paramSelector = new HBox(8, new Label("Parameter:"), paramBox);
        HBox unitSelector = new HBox(8, new Label("Unit:"), unitBox);
        VBox selector = new VBox(8, paramSelector, unitSelector);
        selector.setPadding(new Insets(8, 0, 8, 0)); // отступы

        VBox left = new VBox(8, selector, statisticsBox); // панель с выбором параметра (ед. измерения) и статистикой
        left.setPrefWidth(220);

        BorderPane center = new BorderPane();
        center.setLeft(left); // слева - панель со статистикой
        center.setCenter(chart); // по центру - график
        BorderPane.setMargin(chart, new Insets(8, 0, 0, 12)); // отступы графика

        root.setCenter(center);
        return root;
    }

    private VBox createStatisticsBox() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-border-color: #cfcfcf; -fx-border-radius: 4; -fx-background-color: white;");
        return box;
    }

    // создание графика: по Х - диапазоны значений, по Y - количество результатов
    private BarChart<String, Number> createChart() {
        CategoryAxis xAxis = new CategoryAxis(); // ось Х
        NumberAxis yAxis = new NumberAxis(); // ось Y
        xAxis.setLabel("Value range");
        yAxis.setLabel("Count");

        // создание гистограммы на основе осей X и Y
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false); // анимация перестроения графика
        chart.setBarGap(2);
        chart.setCategoryGap(28);
        chart.setStyle("CHART_COLOR_1: #4f8cc9;");
        chart.setTitle("Value distribution");
        return chart;
    }

    // метод обновляет окно при выборе параметра или ЕИ
    private void updateSelection(
            ExperimentSummary summary,
            VBox statisticsBox,
            BarChart<String, Number> chart,
            MeasurementParam param,
            String unit
    ) {
        // собираем стату именно для выбранного param+unit
        ParamStatistics statistics = buildSelectedStatistics(summary, param, unit);
        updateStatistics(statisticsBox, statistics);
        updateChart(chart, statistics);
    }

    // метод обновляет текстовую статистику
    private void updateStatistics(VBox box, ParamStatistics statistics) {
        box.getChildren().clear();
        if (statistics == null) {
            return;
        }
        box.getChildren().addAll(
                new Label("Parameter: " + statistics.getParam().name()),
                new Label("Unit: " + statistics.getUnit()),
                new Label("Count: " + statistics.getCount()),
                new Label("Min: " + formatNumber(statistics.getMin())),
                new Label("Max: " + formatNumber(statistics.getMax())),
                new Label("Average: " + formatNumber(statistics.getAverage()))
        );
    }

    // метод очищает старый график, строит новые столбцы - обновляет гистограмму
    private void updateChart(BarChart<String, Number> chart, ParamStatistics statistics) {
        chart.getData().clear();
        if (statistics == null || statistics.getValues().isEmpty()) {
            return;
        }

        // создаем серию данных (столбцы) для нового графика
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        int maxCount = 0;

        // Проходим по диапазонам, bin.label() - X, bin.count() - Y. Так для каждого столбца
        for (DistributionBin bin : buildBins(statistics)) {
            maxCount = Math.max(maxCount, bin.count());
            series.getData().add(new XYChart.Data<>(bin.label(), bin.count()));
        }
        chart.getData().add(series);
        configureCountAxis((NumberAxis) chart.getYAxis(), maxCount);
    }

    // разбиение значений на диапазоны
    private List<DistributionBin> buildBins(ParamStatistics statistics) {
        // берем все значения + минимальное + максимальное
        List<Double> values = statistics.getValues();
        double min = statistics.getMin();
        double max = statistics.getMax();

        if (min == max) {
            return List.of(new DistributionBin(formatNumber(min), values.size()));
        } // если все значения одинаковы - создаем один столбец

        // считаем, сколько диапазонов сделать - минимум 1, максимум 8, ceil - округление вверх
        int binCount = Math.min(MAX_BIN_COUNT, Math.max(1, (int) Math.ceil(Math.sqrt(values.size()))));
        binCount = Math.min(binCount, values.size());
        int[] counts = new int[binCount]; // массив счетчиков, каждый элемент - кол-во значений в одном диапазоне
        double binSize = (max - min) / binCount; // ширина одного диапазона

        for (double value : values) {
            int index = (int) ((value - min) / binSize);
            if (index >= binCount) {
                index = binCount - 1; // если индекс вышел за пределы - кладем в последний диапазон
            }
            counts[index]++;
        }

        int finalBinCount = binCount;

        // поток индексов от 0 до binCount - 1
        return java.util.stream.IntStream.range(0, finalBinCount)
                // каждый индекс превращаем в объект DistributionBin
                .mapToObj(index -> {
                    double start = min + index * binSize;
                    double end;
                    if (index == finalBinCount - 1) {
                        end = max;
                    } else {
                        end = start + binSize;
                    }
                    // создаем объект диапазона: подпись (например, 4.00-8.00) + сколько значений попало
                    return new DistributionBin(formatNumber(start) + " - " + formatNumber(end), counts[index]);
                })
                .toList(); // собираем в список
    }

    // метод, чтобы значения по Y были целочисленные
    private void configureCountAxis(NumberAxis axis, int maxCount) {
        axis.setAutoRanging(false);
        axis.setLowerBound(0);
        axis.setUpperBound(Math.max(2, maxCount + 1));
        axis.setTickUnit(1);
        axis.setMinorTickVisible(false);
    }

    // метод собирает список параметров которые есть в стате
    private List<MeasurementParam> getAvailableParams(ExperimentSummary summary) {
        LinkedHashSet<MeasurementParam> params = new LinkedHashSet<>(); // убираем повторы с сохранением порядка
        for (ParamStatistics statistics : summary.getStatistics()) {
            params.add(statistics.getParam()); // проходим по всей стате и добавляем параметры
        }
        return new ArrayList<>(params);
    }

    // метод собирает ЕИ для выбранного параметра
    private List<String> getAvailableUnits(ExperimentSummary summary, MeasurementParam param) {
        LinkedHashSet<String> units = new LinkedHashSet<>(); // убираем повторы с сохранением порядка
        if (param == null) {
            return new ArrayList<>(units); // пустой список
        }

        for (ParamStatistics statistics : summary.getStatistics()) {
            if (statistics.getParam() == param) {
                units.add(normalizeUnit(statistics.getUnit())); // берем только те ЕИ, которые относятся к выбранному параметру
            }
        }
        return new ArrayList<>(units);
    }

    private ParamStatistics buildSelectedStatistics(ExperimentSummary summary, MeasurementParam param, String unit) {
        if (param == null || unit == null) {
            return null;
        }

        String normalizedUnit = normalizeUnit(unit);
        ParamStatistics selectedStatistics = null;

        for (ParamStatistics statistics : summary.getStatistics()) {
            if (statistics.getParam() == param && normalizedUnit.equals(normalizeUnit(statistics.getUnit()))) {
                if (selectedStatistics == null) {
                    selectedStatistics = new ParamStatistics(param, normalizedUnit);
                }

                for (double value : statistics.getValues()) {
                    selectedStatistics.addValue(value);
                }
            }
        }

        return selectedStatistics;
    }

    private String normalizeUnit(String unit) {
        if (unit == null) {
            return "";
        }

        String normalized = unit.trim()
                .replace("В°", "°")
                .replace("Â°", "°")
                .replace('К', 'K')
                .replace('к', 'k')
                .replace('С', 'C')
                .replace('с', 'c');

        if (normalized.equalsIgnoreCase("pH")) {
            return "pH";
        }
        if (normalized.equalsIgnoreCase("K")) {
            return "K";
        }
        if (normalized.equalsIgnoreCase("C")) {
            return "C";
        }
        if (normalized.equalsIgnoreCase("°C")) {
            return "°C";
        }

        return normalized;
    }

    private String formatNumber(double value) {
        return String.format(Locale.ROOT, "%.2f", value); // возвращает число с двумя знаками после точки
    }

    // класс-запись для одного столбца графика - подпись диапазона (Х) и кол-во значений в нем (Y)
    private record DistributionBin(String label, int count) {}
}
