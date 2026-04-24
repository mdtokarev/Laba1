package domain;

import org.junit.jupiter.api.Test;
import validation.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

class RunTest {

    @Test
//    Проверяем что объект класса Run создаётся корректно
    void shouldCreateRunWithValidData() {
        var exp = new Experiment(1,"exp_name", "exp_desc", "exp_owner");
        var run = new Run(2, exp.getId(), "run_name", "run_operator");

        assertEquals("run_name", run.getName());
        assertEquals("run_operator", run.getOperatorName());
        assertEquals(exp.getId(), run.getExperimentId());
    }

    @Test
//    Проверяем корректность получаемого конструктором experimentId, ну мало ли чо))
    void shouldThrowWhenExpIdIsNegative() {
        assertThrows(ValidationException.class, () -> {
           new Run(1,-1, "name", "operator");
        });
    }

    @Test
//    Проверяем валидацию пустого имени запуска
    void shouldThrowWhenRunNameIsEmpty() {
        assertThrows(ValidationException.class, () -> {
            new Run(1, 1,"", "operator");
        });
    }

    @Test
//    Проверяем валидацию длинного имени запуска
    void shouldThrowWhenRunNameTooLong() {
        assertThrows(ValidationException.class, () -> {
            new Run(1, 1, "a".repeat(129), "operator");
        });
    }

    @Test
//    Проверяем валидацию пустого имени оператора запуска
    void shouldThrowWhenOperatorNameIsEmpty() {
        assertThrows(ValidationException.class, () -> {
           new Run(1, 1, "name", "");
        });
    }

    @Test
//    Проверяем валидацию длинного имени оператора запуска
    void shouldThrowWhenOperatorNameTooLong() {
        assertThrows(ValidationException.class, () -> {
           new Run(1, 1,"name", "a".repeat(65));
        });
    }

    @Test
//    Проверяем корректность валидации при смене имени запуска через сеттер
    void shouldThrowWhenSetNameIsEmpty() {
        var run = new Run(1, 1, "name", "operator");
        assertThrows(ValidationException.class, () ->
                run.setName(""));
    }

    @Test
//    Проверяем корректность валидации при смене оператора запуска через сеттер
    void shouldThrowWhenSetOperatorNameIsEmpty() {
        var run = new Run(1 ,1,"name", "operator");
        assertThrows(ValidationException.class, () ->
                run.setOperatorName(""));
    }

    @Test
    void shouldNotUpdateRunWhenValidationFails() {
        var run = new Run(1, 1, "old", "operator");

        assertThrows(ValidationException.class, () ->
                run.update("", "new operator"));

        assertEquals("old", run.getName());
        assertEquals("operator", run.getOperatorName());
    }
}
