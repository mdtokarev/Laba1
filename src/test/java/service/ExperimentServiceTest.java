package service;

import org.junit.jupiter.api.Test;
import validation.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

class ExperimentServiceTest {

    @Test
//    Проверяем что эксперимент кладётся в коллекцию и достаётся оттуда
    void shouldAddAndGetExperimentById() {
        var service = new ExperimentService();

        var experiment = service.add("exp", "desc", "user");

        assertTrue(experiment.getId() > 0);
        assertSame(experiment, service.getById(experiment.getId()));
    }

    @Test
//    Проверяем что для экспериментов генерируются уникальные ID
    void shouldGenerateDifferentIdForDifferentExperiments() {
        var service = new ExperimentService();

        var first = service.add("exp1", "desc1", "user1");
        var second = service.add("exp2", "desc2", "user2");

        assertTrue(first.getId() > 0);
        assertTrue(second.getId() > 0);
        assertTrue(first.getId() != second.getId());
    }

    @Test
//    Проверяем что эксперименты корректно добавляются в коллекцию, и лист возвращает их все
    void listShouldKeepAllExperiments() {
        var service = new ExperimentService();

        var first = service.add("exp1", "desc1", "user1");
        var second = service.add("exp2", "desc2", "user2");

        var experiments = service.list();

        assertEquals(2, experiments.size());
        assertTrue(experiments.contains(first));
        assertTrue(experiments.contains(second));
    }

    @Test
//    Проверяем что сервис корректно обновляет эксперимент
    void shouldUpdateExperiment() {
        var service = new ExperimentService();
        var experiment = service.add("old", "desc", "user");

        var updated = service.update(experiment.getId(), "new", "new desc", "new user");

        assertSame(experiment, updated);
        assertEquals("new", updated.getName());
        assertEquals("new desc", updated.getDescription());
        assertEquals("new user", updated.getOwnerUsername());
    }

    @Test
//    Проверяем что сервис корректно удаляет эксперимент
    void shouldRemoveExperiment() {
        var service = new ExperimentService();
        var experiment = service.add("exp", "desc", "user");

        service.remove(experiment.getId());

        assertEquals(0, service.list().size());
        assertThrows(ValidationException.class, () ->
                service.getById(experiment.getId()));
    }

    @Test
//    Проверяем валидацию отсутствия эксперимента в коллекции
    void shouldThrowWhenExperimentNotFound() {
        var service = new ExperimentService();

        var first = service.add("exp1", "desc1", "user1");
        var second = service.add("exp2", "desc2", "user2");
        var experiments = service.list();

        assertThrows(ValidationException.class, () -> service.getById(3));
    }

    @Test
//    Проверяем валидацию удаления несуществующего эксперимента
    void shouldThrowWhenRemovingMissingExperiment() {
        var service = new ExperimentService();

        var first = service.add("exp1", "desc1", "user1");
        var second = service.add("exp2", "desc2", "user2");
        var experiments = service.list();

        assertThrows(ValidationException.class, () -> service.remove(3));
    }
}

