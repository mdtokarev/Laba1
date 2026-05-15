package ui;

import service.*;
import ui.controller.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class UiMain extends Application {

    @Override
    public void start(Stage stage){
        //Создаем сервисы эксперементов, прогонов и результатов
        ExperimentService experimentService = new ExperimentService();
        RunService runService = new RunService(experimentService);
        RunResultService resultService = new RunResultService(runService);
        AuthService authService = new AuthService();

        //Создаем менеджер для загррузки и сохранения
        DataManager dataManager = new DataManager(experimentService, runService, resultService, authService);

        //Создаем сервис сложных операций удаления
        LabService labService = new LabService(experimentService, runService, resultService);

        //Создаем сервис статистики
        ExperimentSummaryService summaryService = new ExperimentSummaryService(experimentService, runService, resultService);

        //Создаем контроллер UI, контролер связывает кнопки с действиями
        MainController controller = new MainController(stage, experimentService, runService, resultService, dataManager, labService, summaryService);

        //Создаем содержимое окна 1200 на 700
        Scene scene = new Scene(controller.getRoot(), 1200, 720);

        //Насраиваем окно название, окно, показ пользователю
        stage.setTitle("InfoChem Lab Manager");
        stage.setScene(scene);
        stage.show();

        //Если при запуске передан файл автоматически произойдет его загрузка
        if (!getParameters().getRaw().isEmpty()) {
            controller.loadInitialFile(getParameters().getRaw().get(0));
        }
    }
}
