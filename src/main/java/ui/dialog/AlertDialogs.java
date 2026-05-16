package ui.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

//Класс для показа сообщений
public class AlertDialogs {

    //Метод который показывает ошибку
    public void showError(String message) {
        //Окно типа ERROR
        Alert alert = new Alert(Alert.AlertType.ERROR);
        //Заголовок окна
        alert.setTitle("Error");
        //Крупный текст сверху
        alert.setHeaderText("Operation failed");
        //Текст ошибки
        alert.setContentText(message);
        //Показываем окно пользователю пока он его не закроет
        alert.showAndWait();
    }

    //Метод который показывает информационное окно
    public void showInfo(String title, String message) {
        //Окно типа INFORMATION
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        //Ставим заголовок
        alert.setTitle(title);
        //Убираем верхний header
        alert.setHeaderText(null);
        //Пишем сообщение
        alert.setContentText(message);
        //Показываем окно
        alert.showAndWait();
    }

    //Метод спращивает подтверждение соответственно возращает true или false
    public boolean confirm( String message) {
        //Создаем окно подтверждения
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        //Ставим заголовок
        alert.setTitle("Confirm");
        //Крупный текст сверху
        alert.setHeaderText("Please confirm action");
        //Пишем сообщение
        alert.setContentText(message);

        //Показываем окно и получаем какую кнопку нажал пользователь
        Optional<ButtonType> result = alert.showAndWait();

        //Если пользователь нажал ок то возвращаем true, Если Cancel или закрыл окно  false
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
