package ui.view;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ui.viewmodel.ExperimentRow;
import ui.viewmodel.RunResultRow;
import ui.viewmodel.RunRow;


public class MainView {
    //root главный контейнер всего окна в нем будут снопки и 3 таблицы
    private final BorderPane root = new BorderPane();

    //Таблица эксперементов, прогонов, результатов
    private final TableView<ExperimentRow> experimentTable = new TableView<>();
    private final TableView<RunRow> runTable = new TableView<>();
    private final TableView<RunResultRow> resultTable = new TableView<>();

    //Кнопка ручного обновления таблицы
    private final Button refreshButton = new Button("Refresh");

    //Кнопки Save, Save As, Load
    private final Button saveButton = new Button("Save");
    private final Button saveAsButton = new Button("Save As");
    private final Button loadButton = new Button("Load");

    //Кнопки для эксперементов
    private final Button addExperimentButton = new Button("Add Experiment");
    private final Button editExperimentButton = new Button("Edit Experiment");
    private final Button deleteExperimentButton = new Button("Delete Experiment");

    //Кнопки для прогонов
    private final Button addRunButton = new Button("Add Run");
    private final Button editRunButton = new Button("Edit Run");
    private final Button deleteRunButton = new Button("Delete Run");

    //Кнопки для результатов прогонов
    private final Button addResultButton = new Button("Add Result");
    private final Button editResultButton = new Button("Edit Result");
    private final Button deleteResultButton = new Button("Delete Result");

    //Кнопка для статистики
    private final Button summaryButton = new Button("Summary");

    //Настраиваем интерфейс: таблицу эксперементов,прогонов,результатов и собираем все элементы в одно окно
    public MainView() {
        configureExperimentTable();
        configureRunTable();
        configureResultTable();
        configureLayout();
    }

    //Возвращаем главный контейнер интерфейса
    public Parent getRoot() {
        return root;
    }

    //Гетеры для таблиц для эксперементов, прогонов, результатов, чтобы контролер имел доступ к таблице чтобы узнать выбранную строку
    public TableView<ExperimentRow> getExperimentTable() {
        return experimentTable;
    }

    public TableView<RunRow> getRunTable() {
        return runTable;
    }

    public TableView<RunResultRow> getResultTable() {
        return resultTable;
    }

    //Кладем список строк эксперементов, прогонов, результатов и таблица показвает именно их
    public void setExperiments(ObservableList<ExperimentRow> experiments) {
        experimentTable.setItems(experiments);
    }

    public void setRuns(ObservableList<RunRow> runs) {
        runTable.setItems(runs);
    }

    public void setResults(ObservableList<RunResultRow> results) {
        resultTable.setItems(results);
    }

    //гетеры на каждую кнопку чтобы в дальнешем привязать к ней дейстиве
    public Button getRefreshButton() {
        return refreshButton;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public Button getSaveAsButton() {
        return saveAsButton;
    }

    public Button getLoadButton() {
        return loadButton;
    }

    public Button getAddExperimentButton() {
        return addExperimentButton;
    }

    public Button getEditExperimentButton() {
        return editExperimentButton;
    }

    public Button getDeleteExperimentButton() {
        return deleteExperimentButton;
    }

    public Button getAddRunButton() {
        return addRunButton;
    }

    public Button getEditRunButton() {
        return editRunButton;
    }

    public Button getDeleteRunButton() {
        return deleteRunButton;
    }

    public Button getAddResultButton() {
        return addResultButton;
    }

    public Button getEditResultButton() {
        return editResultButton;
    }

    public Button getDeleteResultButton() {
        return deleteResultButton;
    }

    public Button getSummaryButton() {
        return summaryButton;
    }

    //Собираем внешний вид нашего окна
    private void configureLayout() {
        //Панель кнопок верхних
        ToolBar fileToolbar = new ToolBar(refreshButton, saveButton, saveAsButton, loadButton, summaryButton);

        //Панель кнопок эксперемента
        ToolBar experimentToolbar = new ToolBar(addExperimentButton, editExperimentButton, deleteExperimentButton);

        //Панель кнопок прогонов
        ToolBar runToolbar = new ToolBar(addRunButton, editRunButton, deleteRunButton);

        //Панель кнопок результатов
        ToolBar resultToolbar = new ToolBar(addResultButton, editResultButton, deleteResultButton);

        //Создаем вертикальный блок для эксперементов, прогонов, результатов
        VBox experimentBox = new VBox(experimentToolbar, experimentTable);
        VBox runBox = new VBox(runToolbar, runTable);
        VBox resultBox = new VBox(resultToolbar, resultTable);

        //Растягиваем таблицу эксперементов, прогонов, результатов по высоте
        VBox.setVgrow(experimentTable, Priority.ALWAYS);
        VBox.setVgrow(runTable, Priority.ALWAYS);
        VBox.setVgrow(resultTable, Priority.ALWAYS);

        //Делим окно на 3 части
        SplitPane splitPane = new SplitPane(experimentBox, runBox, resultBox);
        splitPane.setDividerPositions(0.34, 0.67);

        //Вверх окна главная панель кнопок
        root.setTop(fileToolbar);
        //Центр окна 3 таблицы
        root.setCenter(splitPane);
        //Отступ 8 пикселей от краев
        root.setPadding(new Insets(8));
    }

    //Настраиваем таблицу эксперемнтов
    private void configureExperimentTable() {
        //Колонки будут растягиваться по ширине таблицы
            experimentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            //Если данных нет, выскакивает текст
            experimentTable.setPlaceholder(new javafx.scene.control.Label("No experiments"));

            //Создаем колонку ID, она работает со строками ExperimentRow
            TableColumn<ExperimentRow, Long> idColumn = new TableColumn<>("ID");
            //Берем значение через гетер
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

            //Создаем колонку name, она работает со строками ExperimentRow
            TableColumn<ExperimentRow, String> nameColumn = new TableColumn<>("Name");
            //Берем значение через гетер
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

            //Создаем колонку description, она работает со строками ExperimentRow
            TableColumn<ExperimentRow, String> descriptionColumn = new TableColumn<>("Description");
             //Берем значение через гетер
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

            //Создаем колонку owner, она работает со строками ExperimentRow
            TableColumn<ExperimentRow, String> ownerColumn = new TableColumn<>("Owner");
            //Берем значение через гетер
            ownerColumn.setCellValueFactory(new PropertyValueFactory<>("ownerUsername"));

            //Создаем колонку created, она работает со строками ExperimentRow
            TableColumn<ExperimentRow, String> createdColumn = new TableColumn<>("Created");
             //Берем значение через гетер
            createdColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

            //Создаем колонку updated, она работает со строками ExperimentRow
            TableColumn<ExperimentRow, String> updatedColumn = new TableColumn<>("Updated");
            //Берем значение через гетер
            updatedColumn.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));

            //Добавляем колонки в таблицу
            experimentTable.getColumns().add(idColumn);
            experimentTable.getColumns().add(nameColumn);
            experimentTable.getColumns().add(descriptionColumn);
            experimentTable.getColumns().add(ownerColumn);
            experimentTable.getColumns().add(createdColumn);
            experimentTable.getColumns().add(updatedColumn);
        }

    //Настраиваем таблицу прогонов
        private void configureRunTable() {
            //Колонки будут растягиваться по ширине таблицы
            runTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            //Если данных нет, выскакивает текст
            runTable.setPlaceholder(new javafx.scene.control.Label("No runs"));

            //Создаем колонку ID, она работает со строками RunRow
            TableColumn<RunRow, Long> idColumn = new TableColumn<>("ID");
            //Берем значение через гетер
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

            //Создаем колонку experimentId, она работает со строками RunRow
            TableColumn<RunRow, Long> experimentIdColumn = new TableColumn<>("Experiment ID");
            //Берем значение через гетер
            experimentIdColumn.setCellValueFactory(new PropertyValueFactory<>("experimentId"));

            //Создаем колонку name, она работает со строками RunRow
            TableColumn<RunRow, String> nameColumn = new TableColumn<>("Name");
            //Берем значение через гетер
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

            //Создаем колонку operator, она работает со строками RunRow
            TableColumn<RunRow, String> operatorColumn = new TableColumn<>("Operator");
            //Берем значение через гетер
            operatorColumn.setCellValueFactory(new PropertyValueFactory<>("operatorName"));

            //Создаем колонку created, она работает со строками RunRow
            TableColumn<RunRow, String> createdColumn = new TableColumn<>("Created");
            //Берем значение через гетер
            createdColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

            //Создаем колонку updated, она работает со строками RunRow
            TableColumn<RunRow, String> updatedColumn = new TableColumn<>("Updated");
            //Берем значение через гетер
            updatedColumn.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));

            //Добавляем колонки в таблицу
            runTable.getColumns().add(idColumn);
            runTable.getColumns().add(experimentIdColumn);
            runTable.getColumns().add(nameColumn);
            runTable.getColumns().add(operatorColumn);
            runTable.getColumns().add(createdColumn);
            runTable.getColumns().add(updatedColumn);
        }

    //Настраиваем таблицу результатов прогонов
        private void configureResultTable() {
            //Колонки будут растягиваться по ширине таблицы
            resultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            //Если данных нет, выскакивает текст
            resultTable.setPlaceholder(new javafx.scene.control.Label("No results"));

            //Создаем колонку id, она работает со строками RunResultRow
            TableColumn<RunResultRow, Long> idColumn = new TableColumn<>("ID");
            //Берем значение через гетер
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

            //Создаем колонку runId, она работает со строками RunResultRow
            TableColumn<RunResultRow, Long> runIdColumn = new TableColumn<>("Run ID");
            //Берем значение через гетер
            runIdColumn.setCellValueFactory(new PropertyValueFactory<>("runId"));

            //Создаем колонку  param, она работает со строками RunResultRow
            TableColumn<RunResultRow, String> paramColumn = new TableColumn<>("Param");
            //Берем значение через гетер
            paramColumn.setCellValueFactory(new PropertyValueFactory<>("param"));

            //Создаем колонку value, она работает со строками RunResultRow
            TableColumn<RunResultRow, Double> valueColumn = new TableColumn<>("Value");
            //Берем значение через гетер
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

            //Создаем колонку unit, она работает со строками RunResultRow
            TableColumn<RunResultRow, String> unitColumn = new TableColumn<>("Unit");
            //Берем значение через гетер
            unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

            //Создаем колонку comment, она работает со строками RunResultRow
            TableColumn<RunResultRow, String> commentColumn = new TableColumn<>("Comment");
            //Берем значение через гетер
            commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));

            //Создаем колонку created, она работает со строками RunResultRow
            TableColumn<RunResultRow, String> createdColumn = new TableColumn<>("Created");
            //Берем значение через гетер
            createdColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

            //Создаем колонку updated, она работает со строками RunResultRow
            TableColumn<RunResultRow, String> updatedColumn = new TableColumn<>("Updated");
            //Берем значение через гетер
            updatedColumn.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));

            //Добавляем колонки в таблицу
            resultTable.getColumns().add(idColumn);
            resultTable.getColumns().add(runIdColumn);
            resultTable.getColumns().add(paramColumn);
            resultTable.getColumns().add(valueColumn);
            resultTable.getColumns().add(unitColumn);
            resultTable.getColumns().add(commentColumn);
            resultTable.getColumns().add(createdColumn);
            resultTable.getColumns().add(updatedColumn);
        }
    }


