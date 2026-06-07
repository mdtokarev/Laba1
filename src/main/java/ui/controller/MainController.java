package ui.controller;

import database.DatabaseSequenceSynchronizer;
import domain.Experiment;
import domain.Run;
import domain.RunResult;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.*;
import ui.dialog.AlertDialogs;
import ui.dialog.EntityDialogs;
import ui.dialog.ExperimentFormData;
import ui.dialog.RunFormData;
import ui.dialog.RunResultFormData;
import ui.mapper.UiModelMapper;
import ui.view.MainView;
import ui.viewmodel.ExperimentRow;
import ui.viewmodel.RunResultRow;
import ui.viewmodel.RunRow;
import validation.ValidationException;

import java.io.File;
import java.io.IOException;
import java.util.Optional;


public class MainController {
    private final Stage stage;

    private final ExperimentService experimentService;
    private final RunService runService;
    private final RunResultService resultService;
    private final DataManager dataManager;
    private final LabService labService;
    private final ExperimentSummaryService summaryService;
    private final AuthService authService;
    private final AccessControlService accessControlService;
    private final boolean databaseEnabled;
    private final DatabaseSequenceSynchronizer sequenceSynchronizer;

    private final MainView view;
    private final UiModelMapper mapper;
    private final AlertDialogs alerts;
    private final EntityDialogs dialogs;

    private String currentFilePath;

//    запускать в окно Ui только зареганных юзеров, удален костыль с owner_id = 1. см строка 188

    public MainController(Stage stage, ExperimentService experimentService, RunService runService, RunResultService resultService,
                          DataManager dataManager, LabService labService, ExperimentSummaryService summaryService,
                          AuthService authService, AccessControlService accessControlService, boolean databaseEnabled,
                          DatabaseSequenceSynchronizer sequenceSynchronizer) {
        this.stage = stage;
        this.experimentService = experimentService;
        this.runService = runService;
        this.resultService = resultService;
        this.dataManager = dataManager;
        this.labService = labService;
        this.summaryService = summaryService;
        this.authService = authService;
        this.accessControlService = accessControlService;
        this.databaseEnabled = databaseEnabled;
        this.sequenceSynchronizer = sequenceSynchronizer;

        this.view = new MainView();
        this.mapper = new UiModelMapper();
        this.alerts = new AlertDialogs();
        this.dialogs = new EntityDialogs();

        //Подключаем действия к кнопкам
        connectActions();

        // показываем имя залогиненного пользователя
        view.setCurrentUserText("User: " + authService.requireCurrentUser().getLogin());

        //Первый раз заполняем таблицы данными из сервисов
        refreshAll();
    }

    //Метод возвращает главнй контейнер окна
    public Parent getRoot() {
        return view.getRoot();
    }

    //Метод чтобы при запуске пользовательского окна мы могли запуститься с файлом
    public void loadInitialFile(String path) {
        if (databaseEnabled) {
            alerts.showInfo("PostgreSQL", "Data is loaded automatically from PostgreSQL.");
            return;
        }
        try {
            dataManager.loadFromFile(path);//Загружаем данные из файла
            currentFilePath = path;//Запоминаем файл как текущий
            refreshAll();//Обновляем таблицы
            alerts.showInfo("Loaded", "Data loaded from:\n" + path);//Показываем пользователю что все загрузилось
            //Ловим если что ошибки
        } catch (IOException e) {
            alerts.showError("File error: " + e.getMessage());
        } catch (ValidationException e) {
            alerts.showError("Invalid file content: " + e.getMessage());
        }
    }

    private void connectActions() {
        //Когда нажали Refresh, вызывается refreshAll
        view.getRefreshButton().setOnAction(event -> runSafely(this::refreshAll));

        //Подкючаем все кнопки к соответствующим методам
        view.getSaveButton().setOnAction(event -> runSafely(this::save));
        view.getSaveAsButton().setOnAction(event -> runSafely(this::saveAs));
        view.getLoadButton().setOnAction(event -> runSafely(this::load));

        view.getAddExperimentButton().setOnAction(event -> runSafely(this::addExperiment));
        view.getEditExperimentButton().setOnAction(event -> runSafely(this::editExperiment));
        view.getDeleteExperimentButton().setOnAction(event -> runSafely(this::deleteExperiment));

        view.getAddRunButton().setOnAction(event -> runSafely(this::addRun));
        view.getEditRunButton().setOnAction(event -> runSafely(this::editRun));
        view.getDeleteRunButton().setOnAction(event -> runSafely(this::deleteRun));

        view.getAddResultButton().setOnAction(event -> runSafely(this::addResult));
        view.getEditResultButton().setOnAction(event -> runSafely(this::editResult));
        view.getDeleteResultButton().setOnAction(event -> runSafely(this::deleteResult));

        view.getSummaryButton().setOnAction(event -> runSafely(this::showSummary));

        //Когда пользователь выбирает эксперимент, обновляется таблица прогонов
        view.getExperimentTable().getSelectionModel().selectedItemProperty()
                .addListener((
                        observable,
                        oldValue,
                        newValue) -> { refreshRunsForSelectedExperiment();
                                                     updateActionButtons(); // доступные кнопки - ?
                        });

        //Когда пользователь выбирает прогон, обновляется таблица результатов
        view.getRunTable().getSelectionModel().selectedItemProperty()
                .addListener((
                        observable,
                        oldValue,
                        newValue) -> { refreshResultsForSelectedRun();
                                               updateActionButtons(); // доступные кнопки - ?
                        });

        view.getResultTable().getSelectionModel().selectedItemProperty()
                .addListener((
                        observable,
                        oldValue, newValue) -> {
            updateActionButtons();
        });
    }

    //Метод полностью обновляет таблицу экспериментов из ExperimentService
    private void refreshAll() {
        Long selectedExperimentId = getSelectedExperimentId();
        Long selectedRunId = getSelectedRunId();
        Long selectResultId = getSelectedResultId();

        refreshDataFromStorage();

        //Каждый эксперемнт переводим в ExperimentRow, делаем список для таблицы, кладем данные в таблицу
        view.setExperiments(FXCollections.observableArrayList(experimentService.list().stream().map(mapper::toExperimentRow).toList()));
        selectExperimentById(selectedExperimentId);

        //После обновления эксперементов обновляем зависимые таблицы
        refreshRunsForSelectedExperiment();
        selectRunById(selectedRunId);
        refreshResultsForSelectedRun();
        selectResultById(selectResultId);
        updateActionButtons();
    }

    private void refreshDataFromStorage() {
        if (!databaseEnabled) {
            return;
        }

        sequenceSynchronizer.syncAll();
        authService.refreshFromRepository();
        experimentService.refreshFromRepository();
        runService.refreshFromRepository();
        resultService.refreshFromRepository();
    }

    //Метод обновляет таблицу прогонов для выбранного эксперимента
    private void refreshRunsForSelectedExperiment() {
        //Смотрим какой эксперемнт выбран
        ExperimentRow selectedExperiment = getSelectedExperiment();

        //Если нечего не выбрано то очищаем таблицы прогов и результатов
        if (selectedExperiment == null) {
            view.setRuns(FXCollections.observableArrayList());
            view.setResults(FXCollections.observableArrayList());
            return;
        }
        //Каждый прогон переводим в RunRow, делаем список для таблицы, кладем данные в таблицу
        view.setRuns(FXCollections.observableArrayList(runService.listByExpId(selectedExperiment.getId()).stream().map(mapper::toRunRow).toList()));

        //После обновления эксперементов обновляем зависимые таблицы
        refreshResultsForSelectedRun();
    }

    //Метод обновляет таблицу результатов для выбранного прогона
    private void refreshResultsForSelectedRun() {
        //Смотрим какой прогон выбран
        RunRow selectedRun = getSelectedRun();

        //Если нечего не выбрано то очищаем таблицу от результатов
        if (selectedRun == null) {
            view.setResults(FXCollections.observableArrayList());
            return;
        }

        //Каждый результат переводим в RunResultRow, делаем список для таблицы, кладем данные в таблицу
        view.setResults(FXCollections.observableArrayList(resultService.listByRunId(selectedRun.getId()).stream().map(mapper::toRunResultRow).toList()));
    }

    //Метод открывает окно добавления эксперимента
    private void addExperiment() {
        //Открываем окно добавления эксперимента
        Optional<ExperimentFormData> result = dialogs.showExperimentDialog("Add experiment", "", "");

        //Если нажали Cancel то ничего не делаем
        if (result.isEmpty()) {
            return;
        }

        //Достаем введенные данные
        ExperimentFormData data = result.get();
        //Передаем данные в сервис там создается настоящий Experiment - новый должен принадлежать текущему юзеру
        experimentService.add(data.getName(), data.getDescription(), authService.requireCurrentUser().getId());

        //Обновляем таблицы
        refreshAll();
    }

    //Метод берет выбранный эксперимент, открывает окно со старыми данными, после OK вызывает обновления
    private void editExperiment() {
        //Требуем, чтобы пользователь выбрал эксперимент
        ExperimentRow selected = requireSelectedExperiment();
        requireCanModifyExperiment(selected.getId()); // проверка прав на редактирование эксперимента (чужой нельзя)
        //Берем настоящий объект из сервиса
        Experiment experiment = experimentService.getById(selected.getId());

        //Открываем окно и передаем старые значения
        Optional<ExperimentFormData> result = dialogs.showExperimentDialog("Edit experiment", experiment.getName(), experiment.getDescription());

        //Если Cancel или закрыл окно, то данных нет
        if (result.isEmpty()) {
            return;
        }

        //Достаем новые данные
        ExperimentFormData data = result.get();

        //Передаем новые данные в сервисы
        experimentService.update(experiment.getId(), data.getName(), data.getDescription());

        //Обновляем таблицу
        refreshAll();
    }

    //Метод берет выбранный эксперимент, спрашивает подтверждение и вызывает удаление эксперемнта со всеми его прогонами и результатами
    private void deleteExperiment() {
        //Берем выбранный эксперемент
        ExperimentRow selected = requireSelectedExperiment();
        requireCanModifyExperiment(selected.getId()); // проверка прав на удаление (чужой нельзя)

        //Спращиваем подтверждение о удалении
        boolean confirmed = alerts.confirm("Delete experiment with all runs and results?");

        //Если Cancel или закрыл окно, то не удаляем
        if (!confirmed) {
            return;
        }

        //Удаляем эксперемнт вместе с прогонами и результатами
        labService.removeExperimentWithChildren(selected.getId());

        //Обновляем таблицу
        refreshAll();
    }

    //Метод добавляет прогон к выбранному эксперименту
    private void addRun() {
        //Проверяем выбран ли эксперемент
        ExperimentRow selectedExperiment = requireSelectedExperiment();
        requireCanModifyExperiment(selectedExperiment.getId()); // проверка прав (можно добавлять run только в свой)

        //Открываем окно добавления прогона
        Optional<RunFormData> result = dialogs.showRunDialog("Add run", "", "");

        //Если пользователь нажал Cancel или закрыл окно  ничего не делаем
        if (result.isEmpty()) {
            return;
        }

        //Достаем данные
        RunFormData data = result.get();

        //Передаем данные в сервис там создается настоящий run
        runService.add(selectedExperiment.getId(), data.getName(), data.getOperatorName());

        //Обновляем таблицу
        refreshRunsForSelectedExperiment();
    }

    //Метод редактирует выбранный прогон
    private void editRun() {
        //Проверяем что выбран прогон
        RunRow selected = requireSelectedRun();
        requireCanModifyRun(selected.getId()); // редактировать чужой нельзя
        //Берем настоящий объект из сервиса
        Run run = runService.getById(selected.getId());

        //Открываем окно редактирования прогона
        Optional<RunFormData> result = dialogs.showRunDialog("Edit run", run.getName(), run.getOperatorName());

        //Если пользователь нажал Cancel или закрыл окно ничего не меняем
        if (result.isEmpty()) {
            return;
        }

        //Достаем данные
        RunFormData data = result.get();

        //Обновляем прогон через сервис
        runService.update(run.getId(), data.getName(), data.getOperatorName());

        //Обновляем таблицу
        refreshRunsForSelectedExperiment();
    }

    //Метод  удаляет выбранный прогон вместе с его результатами
    private void deleteRun() {
        //Проверяем что выбрали прогон
        RunRow selected = requireSelectedRun();
        requireCanModifyRun(selected.getId()); // удалять чужой нельзя

        //Окно подтверждения
        boolean confirmed = alerts.confirm("Delete run with all results?");

        //Если пользователь нажал Cancel или закрыл окно ничего не меняем
        if (!confirmed) {
            return;
        }

        //Удаляем выбранный прогон вместе с результатами
        labService.removeRunWithResults(selected.getId());

        //Обновляем таблицу
        refreshRunsForSelectedExperiment();
    }

    //Метод добавляет результат к выбранному прогону
    private void addResult() {
        //Проверяем выбран ли прогон
        RunRow selectedRun = requireSelectedRun();
        requireCanModifyRun(selectedRun.getId()); // нельзя добавлять к чужому

        //Открываем окно добавления
        Optional<RunResultFormData> result = dialogs.showResultDialog("Add result", null, 0, "", "");

        //Если пользователь нажал Cancel или закрыл окно ничего не меняем
        if (result.isEmpty()) {
            return;
        }

        //Достаем данные
        RunResultFormData data = result.get();

        //Создаем результат через сервис
        resultService.add(selectedRun.getId(), data.getParam(), data.getValue(), data.getUnit(), data.getComment());

        //Обновляем таблицу
        refreshResultsForSelectedRun();
    }

    //Метод редактирует выбранный результат.
    private void editResult() {
        //Проверяем выбран ли прогон
        RunResultRow selected = requireSelectedResult();
        requireCanModifyResult(selected.getId()); // редактировать чужой нельзя
        //Берем настоящий result из сервиса
        RunResult result = resultService.getById(selected.getId());

        //Окно редактирования
        Optional<RunResultFormData> formResult = dialogs.showResultDialog("Edit result", result.getParam(), result.getValue(), result.getUnit(), result.getComment());

        //Если пользователь нажал Cancel или закрыл окно ничего не меняем
        if (formResult.isEmpty()) {
            return;
        }

        //Достаем данные
        RunResultFormData data = formResult.get();

        //Обновляем через сервисы
        resultService.update(result.getId(), data.getParam(), data.getValue(), data.getUnit(), data.getComment());

        //Оновляем таблицу
        refreshResultsForSelectedRun();
    }

    //Метод удаляет выбранный результат напрямую
    private void deleteResult() {
        //Проверяем выбран ли рузультат
        RunResultRow selected = requireSelectedResult();
        requireCanModifyResult(selected.getId()); // нельзя удалять чужой

        //Окно подтверждения
        boolean confirmed = alerts.confirm("Delete selected result?");

        //Если пользователь нажал Cancel или закрыл окно ничего не меняем
        if (!confirmed) {
            return;
        }

        //Удаляем прогон через сервис
        resultService.remove(selected.getId());

        //Обновляем таблицу
        refreshResultsForSelectedRun();
    }

    //Метод показывает статистику по выбранному эксперименту
    private void showSummary() {
        //Проверяем выбран ли эксперемент
        ExperimentRow selected = requireSelectedExperiment();

        //Вызываем сервис статистики
        ExperimentSummary summary = summaryService.buildForExperiment(selected.getId());

        //Создаем объект для сборки текст
        StringBuilder text = new StringBuilder();
        //Добавляем в текст название эксперемента
        text.append("Experiment: ").append(summary.getExperimentName()).append("\n\n");

        //Если нет результатов то No results
        if (summary.getStatistics().isEmpty()) {
            text.append("No results");
            //Если статистика есть проходимся по каждому параметру и добавляем их
        } else {
            for (ParamStatistics statistics : summary.getStatistics()) {
                text.append(statistics.getParam()).append("\n");
                text.append("Count: ").append(statistics.getCount()).append("\n");
                text.append("Min: ").append(statistics.getMin()).append("\n");
                text.append("Max: ").append(statistics.getMax()).append("\n");
                text.append("Average: ").append(statistics.getAverage()).append("\n\n");
            }
        }

        //Показываем окно со статистикой
        alerts.showInfo("Summary", text.toString());
    }

    //Метод сохраняет данные в текущий JSON-файл
    private void save() throws IOException {
        if (databaseEnabled) {
            alerts.showInfo("PostgreSQL", "Data is stored automatically in PostgreSQL.");
            return;
        }
        //Проверяем текущий файл
        if (currentFilePath == null || currentFilePath.isBlank()) {
            //Если текущего файла нет вызываем saveAs
            saveAs();
            return;
        }

        //Если текущий файл есть сохраняем туда
        dataManager.saveToFile(currentFilePath);
        //Показываем сообщение об успехе
        alerts.showInfo("Saved", "Data saved to:\n" + currentFilePath);
    }

    //Метод открывает окно выбора файла, сохраняет данные туда и делает этот путь текущим
    private void saveAs() throws IOException {
        if (databaseEnabled) {
            alerts.showInfo("PostgreSQL", "Data is stored automatically in PostgreSQL.");
            return;
        }
        //Создаем окно выбора файлов
        FileChooser chooser = createJsonFileChooser("Save As");
        //Показываем пользователю окно сохранения файла
        File file = chooser.showSaveDialog(stage);

        //Если пользователь нажал Cancel, файла нет
        if (file == null) {
            return;
        }

        //Запоминаем выбранный путь как текущий файл
        currentFilePath = file.getAbsolutePath();
        //Сохраняем данные в этот файл
        dataManager.saveToFile(currentFilePath);

        //Сообщение об успехе
        alerts.showInfo("Saved", "Data saved to:\n" + currentFilePath);
    }

    //Метод открывает окно выбора JSON-файла, загружает данные через DataManager, обновляет таблицы
    private void load() throws IOException {
        if (databaseEnabled) {
            alerts.showInfo("PostgreSQL", "Data is loaded automatically from PostgreSQL at startup.");
            return;
        }
        //Создаем окно выбора файла
        FileChooser chooser = createJsonFileChooser("Load");
        //Показываем окно открытия файла
        File file = chooser.showOpenDialog(stage);

        //Если пользователь нажал Cancel, файла нет
        if (file == null) {
            return;
        }

        //Загружаем данные из выбранного JSON-файла
        dataManager.loadFromFile(file.getAbsolutePath());
        //Запоминаем файл как текущий
        currentFilePath = file.getAbsolutePath();

        //Обновляем таблицы
        refreshAll();

        //Сообщение об успехе
        alerts.showInfo("Loaded", "Data loaded from:\n" + currentFilePath);
    }

    //Метод создает окно выбора файла
    private FileChooser createJsonFileChooser(String title) {
        //Создаем окно для выбоа файла
        FileChooser chooser = new FileChooser();
        //Ставим заголовок окна
        chooser.setTitle(title);
        //Добавляем фильтр, чтобы удобно выбирать JSON-файлы и фильтр где все файлы
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));

        //Возвращаем готовое окно выбора файла
        return chooser;
    }

//    метод смотрит, что сейчас выбрано в таблицах и решает, какие кнпоки можно нажимать
    private void updateActionButtons() {

        // берем текущий выбранный объект из его таблицы
        ExperimentRow experiment = getSelectedExperiment();
        RunRow run = getSelectedRun();
        RunResultRow result = getSelectedResult();

        // если объект выбран, вызываем canModify, который проверяет права пользователя
        boolean canModifyExperiment = experiment != null && canModifyExperiment(experiment.getId());
        boolean canModifyRun = run != null && canModifyRun(run.getId());
        boolean canModifyResult = result != null && canModifyResult(result.getId());

        // если canModify == false, выключаем кнопки
        view.getEditExperimentButton().setDisable(!canModifyExperiment);
        view.getDeleteExperimentButton().setDisable(!canModifyExperiment);
        view.getAddRunButton().setDisable(!canModifyExperiment); // добавление Run зависит от прав на Experiment

        view.getEditRunButton().setDisable(!canModifyRun);
        view.getDeleteRunButton().setDisable(!canModifyRun);
        view.getAddResultButton().setDisable(!canModifyRun);

        view.getEditResultButton().setDisable(!canModifyResult);
        view.getDeleteResultButton().setDisable(!canModifyResult);
    }

//    методы проверки прав - просто отдают true/false
    private boolean canModifyExperiment(long experimentId) {
        try {
            // требуем регистрации юзера, берем его id, проверяем владение экспериментом
            accessControlService.checkCanModifyExperiment(authService.requireCurrentUser().getId(), experimentId);
            return true; // исключений нет -> объект менять можно -> true
        } catch (ValidationException e) {
            return false; // прав нет -> кнопку выключаем
        }
    }

    private boolean canModifyRun(long runId) {
        try {
            accessControlService.checkCanModifyRun(authService.requireCurrentUser().getId(), runId);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

    private boolean canModifyResult(long resultId) {
        try {
            accessControlService.checkCanModifyResult(authService.requireCurrentUser().getId(), resultId);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }

//    методы защиты действий
    private void requireCanModifyExperiment(long experimentId) {
        // если юзер имеет право -> все ок, выполнение идет дальше, ошибки нет
        // в противном случае бросится Exception
        accessControlService.checkCanModifyExperiment(authService.requireCurrentUser().getId(), experimentId);
    }

    private void requireCanModifyRun(long runId) {
        accessControlService.checkCanModifyRun(authService.requireCurrentUser().getId(), runId);
    }

    private void requireCanModifyResult(long resultId) {
        accessControlService.checkCanModifyResult(authService.requireCurrentUser().getId(), resultId);
    }

    //Метод возвращает выбранную строку из таблицы
    private ExperimentRow getSelectedExperiment() {
        //Берем выбранную строку из таблицы экспериментов, если ничего не выбрано, вернется null
        return view.getExperimentTable().getSelectionModel().getSelectedItem();
    }

    //Метод возвращает выбранную строку из таблицы
    private RunRow getSelectedRun() {
        //Берем выбранную строку из таблицы прогонов, если ничего не выбрано, вернется null
        return view.getRunTable().getSelectionModel().getSelectedItem();
    }

    //Метод возвращает выбранную строку из таблицы
    private RunResultRow getSelectedResult() {
        //Берем выбранную строку из таблицы результатов, если ничего не выбрано, вернется null
        return view.getResultTable().getSelectionModel().getSelectedItem();
    }

//    вспомогательные методы для refresh - берут выбранную строку, возвращают ее id/null
    private Long getSelectedExperimentId() {
        ExperimentRow selected = getSelectedExperiment();
        if (selected == null) {
            return null;
        } else {
            return selected.getId();
        }
    }

    private Long getSelectedRunId() {
        RunRow selected = getSelectedRun();
        if (selected == null) {
            return null;
        } else {
            return selected.getId();
        }
    }

    private Long getSelectedResultId() {
        RunResultRow selected = getSelectedResult();
        if (selected == null) {
            return null;
        } else {
            return selected.getId();
        }    }

//    вспомогательные методы для refresh - ищем запомненную строку в таблице и выбираем ее обратно (если есть)
    private void selectExperimentById(Long id) {
        if (id == null) {
            return;
        }

        for (ExperimentRow row : view.getExperimentTable().getItems()) {
            if (row.getId() == id) {
                view.getExperimentTable().getSelectionModel().select(row);
                return;
            }
        }
    }

    private void selectRunById(Long id) {
        if (id == null) {
            return;
        }

//        ищем id, совпадающий с нужным нам - выбираем ее, выходим из метода (подсветка выбранной до рефреш строки)
        for (RunRow row : view.getRunTable().getItems()) {
            if (row.getId() == id) {
                view.getRunTable().getSelectionModel().select(row);
                return;
            }
        }
    }

    private void selectResultById(Long id) {
        if (id == null) {
            return;
        }

        for (RunResultRow row : view.getResultTable().getItems()) {
            if (row.getId() == id) {
                view.getResultTable().getSelectionModel().select(row);
                return;
            }
        }
    }

    //Метод проверяет выбрана строка или нет
    private ExperimentRow requireSelectedExperiment() {
        //Берем выбранный эксперимент
                ExperimentRow selected = getSelectedExperiment();

       // Если ничего не выбрано, выбрасываем ошибку
        if (selected == null) {
            throw new ValidationException("Select experiment first");
        }
        //Если строка выбрана, возвращаем ее
        return selected;
    }

    //Метод проверяет выбрана строка или нет
    private RunRow requireSelectedRun() {
        //Берем выбранный прогон
        RunRow selected = getSelectedRun();

        // Если ничего не выбрано, выбрасываем ошибку
        if (selected == null) {
            throw new ValidationException("Select run first");
        }
        //Если строка выбрана, возвращаем ее
        return selected;
    }

    //Метод проверяет выбрана строка или нет
    private RunResultRow requireSelectedResult() {
        //Берем выбранный результат
        RunResultRow selected = getSelectedResult();

        // Если ничего не выбрано, выбрасываем ошибку
        if (selected == null) {
            throw new ValidationException("Select result first");
        }
        //Если строка выбрана, возвращаем ее
        return selected;
    }

    //Метод безопасно выполняет действие кнопки
    private void runSafely(Action action) {
        try {
            //Для каждого действия ловим ошибки и выбрасываем соответствующую ошибку
            action.run();
        } catch (ValidationException e) {
            alerts.showError(e.getMessage());
        } catch (NumberFormatException e) {
            alerts.showError("Value must be a number");
        } catch (IOException e) {
            alerts.showError("File error: " + e.getMessage());
        } catch (RuntimeException e) {
            alerts.showError("Unexpected error: " + e.getMessage());
        }
    }

    //Наш маленький интерфейс нужен чтобы передавать методы как парметры в runSafely
    private interface Action {
        void run() throws IOException;
    }
}

