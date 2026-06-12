package ui;

import service.*;
import ui.controller.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.dialog.AlertDialogs;
import ui.dialog.AuthDialog;
import ui.dialog.AuthDialogResult;

import java.io.IOException;

// точка входа в приложение, собранное через ApplicationService
public class UiMain extends Application {
    private static final String USERS_FILE_PATH = "users.json";

    @Override
    public void start(Stage stage) {
        ApplicationServices services = new ApplicationServices(); // получаем готовую конфигурацию приложения
        AuthService authService = services.getAuthService();
        DataManager dataManager = services.getDataManager();
        StorageMode storageMode = services.getStorageMode();
        AlertDialogs alerts = new AlertDialogs();

        loadUsersOnStart(dataManager, storageMode, alerts);

//        открываем главное окно только после авторизации
        if (!authenticate(authService, dataManager, storageMode, alerts)) {
            stage.close();
            return;
        }

        MainController controller = new MainController(
                stage,
                services.getExperimentService(),
                services.getRunService(),
                services.getRunResultService(),
                services.getLabService(),
                services.getSummaryService(),
                authService,
                services.getAccessControlService(),
                storageMode,
                () -> authenticate(authService, dataManager, storageMode, alerts)
        );

        Scene scene = new Scene(controller.getRoot(), 1200, 720);
        stage.setTitle("InfoChem Lab Manager");
        stage.setScene(scene);
        stage.show();

        if (!services.getStorageMode().isDatabase() && !getParameters().getRaw().isEmpty()) {
            controller.loadInitialFile(getParameters().getRaw().get(0));
        }
    }

    private void loadUsersOnStart(DataManager dataManager, StorageMode storageMode, AlertDialogs alerts) {
        if (storageMode.isDatabase()) {
            return;
        }

        try {
            dataManager.loadUsersFromFile(USERS_FILE_PATH);
        } catch (IOException e) {
            alerts.showError("Could not load users file: " + e.getMessage());
        } catch (RuntimeException e) {
            alerts.showError("Invalid users file: " + e.getMessage());
        }
    }

    private void saveUsers(DataManager dataManager, StorageMode storageMode) {
        if (storageMode.isDatabase()) {
            return;
        }

        try {
            dataManager.saveUsersToFile(USERS_FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Could not save users file: " + e.getMessage(), e);
        }
    }

//    сценарий входа в приложение перед открытием главного окна
    private boolean authenticate(AuthService authService, DataManager dataManager, StorageMode storageMode, AlertDialogs alerts) {

        AuthDialog dialog = new AuthDialog(); // создается форма ввода логина и пароля
        while (authService.getCurrentUser() == null) {
            AuthDialogResult result = dialog.show();
            if (result == null) {
                return false; // юзер не захотел входить -> не открываем главное окно
            }

            try {
                if ("register".equals(result.getAction())) {
                    authService.register(result.getLogin(), result.getPassword()); // вызываем регистрацию юзера
                    saveUsers(dataManager, storageMode);
                }
                authService.login(result.getLogin(), result.getPassword()); // вызывается всегда - логиним юзера
            } catch (RuntimeException e) {
                alerts.showError(e.getMessage()); // после ошибки окно не закрывается, даем еще попытки ввода
            }
        }
//        все успешно, если установился currentUser, цикл while закончен, юзер вошел
        return true;
    }
}
