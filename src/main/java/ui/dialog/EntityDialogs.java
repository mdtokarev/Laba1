package ui.dialog;

import domain.MeasurementParam;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.Optional;

//Класс для создания форм окон в пользовательском интерфейсе
public class EntityDialogs {

    //Показывает окно для добавления или редактирования эксперемента. Когда нажимаешь ок Optional с данными если Cancel то пусто
    public Optional<ExperimentFormData> showExperimentDialog(String title, String name, String description) {
        //Создаем диалог, который в итоге вернет ExperimentFormData
        Dialog<ExperimentFormData> dialog = new Dialog<>();
        //Ставим заголовок и убираем header
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        //Поле ввода имени, valueOrEmpty(name) нужно, чтобы вместо null поставить пустую строку
        TextField nameField = new TextField(valueOrEmpty(name));
        //Поле для описания
        TextArea descriptionArea = new TextArea(valueOrEmpty(description));

        //Описание высотой 3 строчки
        descriptionArea.setPrefRowCount(3);

        //Создаем сетку для окна
        GridPane grid = createGrid();
        //Добавляем имя и поля ввода
        grid.add(new javafx.scene.control.Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        //Добавляем описание и поля ввода
        grid.add(new javafx.scene.control.Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);


        //Добовляем сетку в окно
        dialog.getDialogPane().setContent(grid);
        //Добавляем кнопки ок и Cancel
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        //Говорим что вернуть поле закрытия окна, если ок то создаем объект с нововеденными данными, если Cancel то нечего
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new ExperimentFormData(nameField.getText(), descriptionArea.getText());
            }

            return null;
        });

        //Возвращаем окно и результат
        return dialog.showAndWait();
    }

    //Показывает окно для добавления или редактирования прогона. Когда нажимаешь ок Optional с данными если Cancel то пусто
    public Optional<RunFormData> showRunDialog(String title, String name, String operatorName) {
        //Создаем диалог, который в итоге вернет RunFormData
        Dialog<RunFormData> dialog = new Dialog<>();
        //Ставим заголовок и убираем header
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        //Поле ввода имени, valueOrEmpty(name) нужно, чтобы вместо null поставить пустую строку
        TextField nameField = new TextField(valueOrEmpty(name));
        //Поле для владельца прогона
        TextField operatorField = new TextField(valueOrEmpty(operatorName));

        //Создаем сетку для окна
        GridPane grid = createGrid();
        //Добавляем имя и поля ввода
        grid.add(new javafx.scene.control.Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        //Добавляем владельца прогона и поля ввода
        grid.add(new javafx.scene.control.Label("Operator:"), 0, 1);
        grid.add(operatorField, 1, 1);

        //Добовляем сетку в окно
        dialog.getDialogPane().setContent(grid);
        //Добавляем кнопки ок и Cancel
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        //Говорим что вернуть поле закрытия окна, если ок то создаем объект с нововеденными данными, если Cancel то нечего
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new RunFormData(nameField.getText(), operatorField.getText());
            }

            return null;
        });

        //Возвращаем окно и результат
        return dialog.showAndWait();
    }

    //Показывает окно для добавления или редактирования результата прогона. Когда нажимаешь ок Optional с данными если Cancel то пусто
    public Optional<RunResultFormData> showResultDialog(String title, MeasurementParam param, double value, String unit, String comment) {
        //Создаем диалог, который в итоге вернет RunResultFormData
        Dialog<RunResultFormData> dialog = new Dialog<>();
        //Ставим заголовок и убираем header
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        //Выпадает список для выбора параметра
        ComboBox<MeasurementParam> paramBox = new ComboBox<>();
        //Добавляем туда все варианты enum
        paramBox.getItems().addAll(MeasurementParam.values());
        //Если параметр не передан, по умолчанию ставим pH, если редактируем существующий результат, ставим старое значение
        paramBox.setValue(param == null ? MeasurementParam.pH : param);

        //Поле ввода значения , String.valueOf(value) нужен, чтобы перевести число double в текст
        TextField valueField = new TextField(String.valueOf(value));
        //Поле ввода елениц измерения
        TextField unitField = new TextField(valueOrEmpty(unit));
        //Поле ввода комментария
        TextArea commentArea = new TextArea(valueOrEmpty(comment));
        //Настраиваем высоту поля для комментария
        commentArea.setPrefRowCount(3);

        //Добовляем сетку в окно
        GridPane grid = createGrid();
        //Добавляем параметр и выбор списком
        grid.add(new javafx.scene.control.Label("Parameter:"), 0, 0);
        grid.add(paramBox, 1, 0);
        //Добавляем значение и поле ввода
        grid.add(new javafx.scene.control.Label("Value:"), 0, 1);
        grid.add(valueField, 1, 1);
        //Добавляем еденицы измерения и поле ввода
        grid.add(new javafx.scene.control.Label("Unit:"), 0, 2);
        grid.add(unitField, 1, 2);
        //Добавляем комментарий и поле ввода
        grid.add(new javafx.scene.control.Label("Comment:"), 0, 3);
        grid.add(commentArea, 1, 3);

        //Добовляем сетку в окно
        dialog.getDialogPane().setContent(grid);
        //Добавляем кнопки ок и Cancel
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        //Говорим что вернуть поле закрытия окна, если ок то создаем объект с нововеденными данными, если Cancel то нечего
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                double parsedValue = Double.parseDouble(valueField.getText());
                return new RunResultFormData(paramBox.getValue(), parsedValue, unitField.getText(), commentArea.getText());
            }

            return null;
        });

        //Возвращаем окно и результат
        return dialog.showAndWait();
    }

    //Метод вспомогательный для создания окна
    private GridPane createGrid() {
        //Создаем сетку
        GridPane grid = new GridPane();

        //Горизонтальный отступ между колонками
        grid.setHgap(8);
        //Вертикальный отступ между строками
        grid.setVgap(8);
        //Внутренний отступ окна
        grid.setPadding(new Insets(12));

        //Возращаем сетку
        return grid;
    }

    //Метод защищает от null
    private String valueOrEmpty(String value) {
        //Если значение null то возращаем пустую строку
        if (value == null) {
            return "";
        }

        //Возвращаем значение
        return value;
    }
}


