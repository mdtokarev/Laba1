package ui;

import service.*;
import ui.controller.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.dialog.AlertDialogs;
import ui.dialog.AuthDialog;
import ui.dialog.AuthDialogResult;

// точка входа в приложение, собранное через ApplicationService
public class UiMain extends Application {

    @Override
    public void start(Stage stage) {
        ApplicationServices services = new ApplicationServices(); // получаем готовую конфигурацию приложения
        AuthService authService = services.getAuthService();
        AlertDialogs alerts = new AlertDialogs();

//        открываем главное окно только после авторизации
        if (!authenticate(authService, alerts)) {
            stage.close();
            return;
        }

        MainController controller = new MainController(
                stage,
                services.getExperimentService(),
                services.getRunService(),
                services.getRunResultService(),
                services.getDataManager(),
                services.getLabService(),
                services.getSummaryService(),
                authService,
                services.getAccessControlService(),
                services.isDatabaseEnabled()
        );

        Scene scene = new Scene(controller.getRoot(), 1200, 720);
        stage.setTitle("InfoChem Lab Manager");
        stage.setScene(scene);
        stage.show();

        if (!services.isDatabaseEnabled() && !getParameters().getRaw().isEmpty()) {
            controller.loadInitialFile(getParameters().getRaw().get(0));
        }
    }

//    сценарий входа в приложение перед открытием главного окна
    private boolean authenticate(AuthService authService, AlertDialogs alerts) {

        AuthDialog dialog = new AuthDialog(); // создается форма ввода логина и пароля
        while (authService.getCurrentUser() == null) {
            AuthDialogResult result = dialog.show();
            if (result == null) {
                return false; // юзер не захотел входить -> не открываем главное окно
            }

            try {
                if ("register".equals(result.getAction())) {
                    authService.register(result.getLogin(), result.getPassword()); // вызываем регистрацию юзера
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
