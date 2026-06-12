package ui.dialog;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class AuthDialog {

//    главный метод - открывает окно взаимодействия, которое в итоге возвращает объект с данными (логин пароль)
    public AuthDialogResult show() {
        Dialog<AuthDialogResult> dialog = new Dialog<>();
        dialog.setTitle("Login");
        dialog.setHeaderText("Login or register");

        TextField loginField = new TextField(); // обычное поле для ввода
        PasswordField passwordField = new PasswordField(); // поле где символы скрываются

//        собираем внешний вид окна. GridPane - сетка по строкам и столбцам
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.add(new Label("Login:"), 0, 0);
        grid.add(loginField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);

//       создаем три действия, обрабатываемые в одном окне
        ButtonType loginButton = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        ButtonType registerButton = new ButtonType("Register", ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(loginButton, registerButton, ButtonType.CANCEL);

//        составленную сетку grid делаем содержимым окна
        dialog.getDialogPane().setContent(grid);

        Button login = (Button) dialog.getDialogPane().lookupButton(loginButton);
        Button register = (Button) dialog.getDialogPane().lookupButton(registerButton);

//        блокировка пустого ввода - пока не введено логин и пароль, нельзя пройти дальше
        login.disableProperty().bind(loginField.textProperty().isEmpty().or(passwordField.textProperty().isEmpty()));
        register.disableProperty().bind(login.disableProperty());

//        собираем ввод - что нажал пользователь и с какими данными
        dialog.setResultConverter(button -> {
            if (button == loginButton) {
                return new AuthDialogResult("login", loginField.getText(), passwordField.getText());
            }
            if (button == registerButton) {
                return new AuthDialogResult("register", loginField.getText(), passwordField.getText());
            }
            return null;
        });

//        показываем окно, ждем ввод, возвращаем его результаты; если диалог ничего не вернул -> null
        return dialog.showAndWait().orElse(null);
    }
}
