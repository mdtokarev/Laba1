package domain;

import org.junit.jupiter.api.Test;
import validation.ValidationException;
import static org.junit.jupiter.api.Assertions.*;

class ExperimentTest {

    @Test
//    Проверяем что объект класса Эксперимент создаётся корректно
    void shouldCreateExpWithValidData() {
        var exp = new Experiment(1,"name", "desc", "user");

        assertEquals(1, exp.getId());
        assertEquals("name", exp.getName());
        assertEquals("desc", exp.getDescription());
        assertEquals("user", exp.getOwnerUsername());
    }

    @Test
//    Проверяем валидацию пустого имени
    void shouldThrowWhenNameIsEmpty() {
        assertThrows(ValidationException.class, () -> {
           new Experiment(1,"", "desc", "user");
        });
    }

    @Test
//    Проверяем валидацию длинного имени
    void shouldThrowWhenNameTooLong() {
        assertThrows(ValidationException.class, () -> {
            new Experiment(1, "a".repeat(129), "desc", "user");
        });
    }

    @Test
//    Проверяем валидацию длинного описания
    void shouldThrowWhenDescriptionTooLong() {
        assertThrows(ValidationException.class, () -> {
            new Experiment(1, "name", "a".repeat(513), "user");
        });
    }

    @Test
//    Проверяем валидацию пустого имени владельца
    void shouldThrowWhenOwnerUsernameIsEmpty() {
        assertThrows(ValidationException.class, () -> {
            new Experiment(1, "name", "desc", "");
        });
    }

    @Test
//    Проверяем валидацию длинного имени владельца
    void shouldThrowWhenOwnerUsernameTooLong() {
        assertThrows(ValidationException.class, () -> {
            new Experiment(1, "name", "desc", "a".repeat(129));
        });
    }

    @Test
//    Проверяем что при смене имени через сеттер валидация происходит корректно
    void shouldThrowWhenSetNameIsEmpty() {
        var exp = new Experiment(1, "name", "desc", "user");
        assertThrows(ValidationException.class, () ->
                exp.setName(""));
    }

    @Test
//    Проверяем что при смене имени владельца через сеттер валидация проходит корректно
    void shouldThrowWhenSetOwnerUsernameIsEmpty() {
        var exp = new Experiment(1, "name", "desc", "user");
        assertThrows(ValidationException.class, () ->
                exp.setOwnerUsername(""));
    }

    @Test
/*    Проверяем, что эксперимент не будет обновлён, если хотя бы один
      параметр будет внесён некорректно */
    void shouldNotUpdateExperimentWhenValidationFails() {
        var exp = new Experiment(1, "old", "desc", "user");

        assertThrows(ValidationException.class, () ->
                exp.update("","new desc", "new user"));

        assertEquals("old", exp.getName());
        assertEquals("desc", exp.getDescription());
        assertEquals("user", exp.getOwnerUsername());
    }
}
