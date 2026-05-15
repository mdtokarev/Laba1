package storage;

import org.junit.jupiter.api.Test;
import validation.ValidationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SnapshotValidatorTest {
    private final SnapshotValidator validator = new SnapshotValidator();

    @Test
    //Проверка что snapshot валиден
    void shouldValidateCorrectSnapshot() {
        assertDoesNotThrow(() -> validator.validate(createValidSnapshot()));
    }

    @Test
    //Проверка что эксперемент не null
    void shouldThrowWhenExperimentsAreMissing() {
        DataSnapshot snapshot = createValidSnapshot();
        snapshot.setExperiments(null);

        assertThrows(ValidationException.class, () -> validator.validate(snapshot));
    }

    @Test
    //Проверка что нет повторяющихся ID
    void shouldThrowWhenExperimentIdsAreDuplicated() {
        DataSnapshot snapshot = createValidSnapshot();
        snapshot.setExperiments(List.of(
                new ExperimentData(1L, "Exp 1", null, 1L,
                        "2026-04-21T10:00:00Z", "2026-04-21T10:00:00Z"),
                new ExperimentData(1L, "Exp 2", null, 1L,
                        "2026-04-21T10:00:00Z", "2026-04-21T10:00:00Z")
        ));

        assertThrows(ValidationException.class, () -> validator.validate(snapshot));
    }

    @Test
//Проверка что запуск ссылается на реальный эксперемент
    void shouldThrowWhenRunReferencesMissingExperiment() {
        DataSnapshot snapshot = createValidSnapshot();
        snapshot.setRuns(List.of(
                new RunData(1L, 999L, "Run", "operator",
                        "2026-04-21T10:00:00Z", "2026-04-21T10:00:00Z")
        ));

        assertThrows(ValidationException.class, () -> validator.validate(snapshot));
    }

    @Test
    //Проверка что результат ссылается на рельный прогон
    void shouldThrowWhenResultReferencesMissingRun() {
        DataSnapshot snapshot = createValidSnapshot();
        snapshot.setRunResults(List.of(
                new RunResultData(1L, 999L, "pH", 7.0, "pH", null,
                        "2026-04-21T10:00:00Z", "2026-04-21T10:00:00Z")
        ));

        assertThrows(ValidationException.class, () -> validator.validate(snapshot));
    }

    @Test
    //Проверка что pH в реальном диапазоне
    void shouldThrowWhenPhIsIncorrect() {
        DataSnapshot snapshot = createValidSnapshot();
        snapshot.setRunResults(List.of(
                new RunResultData(1L, 1L, "pH", 20.0, "pH", null,
                        "2026-04-21T10:00:00Z", "2026-04-21T10:00:00Z")
        ));

        assertThrows(ValidationException.class, () -> validator.validate(snapshot));
    }

    @Test
    //Проверяем на правильный формат даты
    void shouldThrowWhenDateIsInvalid() {
        DataSnapshot snapshot = createValidSnapshot();
        snapshot.setExperiments(List.of(
                new ExperimentData(1L, "Exp", "desc", 1L,
                        "bad-date", "2026-04-21T10:00:00Z")
        ));

        assertThrows(ValidationException.class, () -> validator.validate(snapshot));
    }

    @Test
    //Проверка что время создания раньше времени изменения
    void shouldThrowWhenUpdatedAtBeforeCreatedAt() {
        DataSnapshot snapshot = createValidSnapshot();
        snapshot.setExperiments(List.of(
                new ExperimentData(1L, "Exp", "desc", 1L,
                        "2026-04-21T10:05:00Z", "2026-04-21T10:00:00Z")
        ));

        assertThrows(ValidationException.class, () -> validator.validate(snapshot));
    }

    private DataSnapshot createValidSnapshot() {
        return new DataSnapshot(
                List.of(new ExperimentData(1L, "Exp", "desc", 1L,
                        "2026-04-21T10:00:00Z", "2026-04-21T10:00:00Z")),
                List.of(new RunData(1L, 1L, "Run", "operator",
                        "2026-04-21T10:00:00Z", "2026-04-21T10:00:00Z")),
                List.of(new RunResultData(1L, 1L, "pH", 7.0, "pH", "ok",
                        "2026-04-21T10:00:00Z", "2026-04-21T10:00:00Z"))
        );
    }
}
