package domain;

import org.junit.jupiter.api.Test;
import validation.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

class RunResultTest {

    @Test
//    Проверяем что объект класса RunResult создаётся корректно
    void shouldCreateRunResultWithValidData() {
        var exp = new Experiment(1,"exp_name", "exp_desc", "exp_owner");
        var run = new Run(2, exp.getId(), "run name", "run operator");
        var rr = new RunResult(3, run.getId(), MeasurementParam.pH, 7.0, "pH", "comment");

        assertEquals(3, rr.getId());
        assertEquals(run.getId(), rr.getRunId());
        assertEquals(MeasurementParam.pH, rr.getParam());
        assertEquals(7.0, rr.getValue());
        assertEquals("pH", rr.getUnit());
        assertEquals("comment", rr.getComment());
    }

    @Test
//    Проверяем корректность получаемого конструктором runId
    void shouldThrowWhenRunIdIsNegative() {
        assertThrows(ValidationException.class, () -> {
           new RunResult(1,-1, MeasurementParam.pH, 1, "pH", "");
        });
    }

    @Test
//    Проверяем валидацию единиц измерения
    void shouldThrowWhenUnitIsEmpty() {
        assertThrows(ValidationException.class, () -> {
            new RunResult(1, 1, MeasurementParam.pH, 1, "", "");
        });
    }

    @Test
//    Проверяем валидацию длинного комментария
    void shouldThrowWhenCommentTooLong() {
        assertThrows(ValidationException.class, () -> {
           new RunResult(1, 1, MeasurementParam.pH, 1, "pH", "a".repeat(129));
        });
    }

    @Test
//    Проверяем валидацию установки единиц измерения через сеттер
    void shouldThrowWhenSetUnitIsEmpty() {
        var rr = new RunResult(1, 1, MeasurementParam.pH, 1, "pH", "comm");
        assertThrows(ValidationException.class, () ->
                rr.setUnit(""));
    }

    @Test
//    Проверяем валидацию установки длинного комментария через сеттер
    void shouldThrowWhenSetCommentTooLong() {
        var rr = new RunResult(1, 1, MeasurementParam.pH, 1, "pH", "");
        assertThrows(ValidationException.class, () ->
                rr.setComment("a".repeat(129)));
    }

    @Test
//    Проверяем валидацию отрицательного рН
    void shouldThrowWhenPhIsNegative() {
        assertThrows(ValidationException.class, () ->
                new RunResult(1, 1, MeasurementParam.pH, -1.0, "pH", "comment"));
    }

    @Test
//    Проверяем валидацию слишком высокого рН
    void shouldThrowWhenPhIsTooHigh() {
        assertThrows(ValidationException.class, () ->
                new RunResult(1, 1, MeasurementParam.pH, 15.0, "pH", "comment"));
    }

    @Test
//    Проверяем, что не кидает исключение на отрицательную температуру
    void shouldNotThrowWhenTemperatureIsNegative() {
        var result = new RunResult(1, 1, MeasurementParam.Temperature, -10.0, "C", "cold");
        assertEquals(-10.0, result.getValue());
    }

    @Test
//    Проверяем, что через сеттер ставится отрицательная температура
    void shouldSetNegativeTemperature() {
        var result = new RunResult(1, 1, MeasurementParam.Temperature, 25.0, "C", "comment");
        result.setValue(-10.0);
        assertEquals(-10.0, result.getValue());
    }
    @Test
//    Проверяем валидацию отрицательной концентрации
    void shouldThrowWhenConcentrationIsNegative() {
        assertThrows(ValidationException.class, () ->
                new RunResult(1, 1, MeasurementParam.Concentration, -1.0, "mg/L", "comment"));
    }

    @Test
//    Проверяем валидацию установки отрицательного рН через сеттер
    void shouldThrowWhenSetNegativePh() {
        var result = new RunResult(1, 1, MeasurementParam.pH, 7.0, "pH", "comm");
        assertThrows(ValidationException.class, () -> {
           result.setValue(-1);
        });
    }

    @Test
//    Проверяем валидацию установки отрицательной концентрации через сеттер
    void shouldThrowWhenSetNegativeConcentration() {
        var result = new RunResult(1, 1, MeasurementParam.Concentration, 50, "g/L", "comm");
        assertThrows(ValidationException.class, () -> {
           result.setValue(-10);
        });
    }

    @Test
//    Проверяем что нельзя сменить параметр если старые измерения к нему не подходят
    void shouldNotChangeParamWhenOldValueIsInvalidToIt() {
        var rr = new RunResult(1,1,MeasurementParam.Temperature, -10, "C", "comm");
        assertThrows(ValidationException.class, () ->
                rr.setParam(MeasurementParam.pH));
        assertEquals(MeasurementParam.Temperature, rr.getParam());
    }

    @Test
    void shouldNotUpdateRunResultWhenValidationFails() {
        var rr = new RunResult(1,1, MeasurementParam.pH, 7.0, "pH", "comm");

        assertThrows(ValidationException.class, () ->
                rr.update(MeasurementParam.pH, 20, "pOH", "new comm"));

        assertEquals(MeasurementParam.pH, rr.getParam());
        assertEquals(7, rr.getValue());
        assertEquals("pH", rr.getUnit());
        assertEquals("comm", rr.getComment());
    }
}
