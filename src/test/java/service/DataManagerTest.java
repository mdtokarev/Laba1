package service;

import domain.MeasurementParam;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import validation.ValidationException;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DataManagerTest {

    @TempDir
    //Создаем временную папку
    Path tempDir;

    @Test
    //Проверяем что данные коректно сохраняются и коректно востанавливаются
    void shouldSaveAndLoadData() throws Exception {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var authService = new AuthService();
        var dataManager = new DataManager(experimentService, runService, resultService, authService);

        var experiment = experimentService.add("Exp", "desc", 1);
        var run = runService.add(experiment.getId(), "Run", "operator");
        resultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", "ok");

        Path file = tempDir.resolve("data.json");
        dataManager.saveToFile(file.toString());

        var loadedExperimentService = new ExperimentService();
        var loadedRunService = new RunService(loadedExperimentService);
        var loadedResultService = new RunResultService(loadedRunService);
        var loadedAuthService = new AuthService();
        var loadedDataManager = new DataManager(loadedExperimentService, loadedRunService, loadedResultService, loadedAuthService);

        loadedDataManager.loadFromFile(file.toString());

        assertEquals(1, loadedExperimentService.list().size());
        assertEquals(1, loadedRunService.list().size());
        assertEquals(1, loadedResultService.list().size());
        assertEquals("Exp", loadedExperimentService.getById(1).getName());
        assertEquals("Run", loadedRunService.getById(1).getName());
        assertEquals(7.0, loadedResultService.getById(1).getValue());
    }

    @Test
    //Проверка что после загрузки данных из JSON ID коректно создаются дальше, а не начинаются с 0
    void shouldContinueIdsAfterLoad() throws Exception {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var authService = new AuthService();
        var dataManager = new DataManager(experimentService, runService, resultService, authService);

        var experiment = experimentService.add("Exp", null, 1);
        var run = runService.add(experiment.getId(), "Run", "operator");
        resultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", null);

        Path file = tempDir.resolve("data.json");
        dataManager.saveToFile(file.toString());

        var loadedExperimentService = new ExperimentService();
        var loadedRunService = new RunService(loadedExperimentService);
        var loadedResultService = new RunResultService(loadedRunService);
        var loadedAuthService = new AuthService();
        var loadedDataManager = new DataManager(loadedExperimentService, loadedRunService, loadedResultService, loadedAuthService);

        loadedDataManager.loadFromFile(file.toString());

        var nextExperiment = loadedExperimentService.add("New exp", null, 2);
        var nextRun = loadedRunService.add(1, "New run", "operator2");
        var nextResult = loadedResultService.add(1, MeasurementParam.Temperature, 20.0, "C", null);

        assertEquals(2, nextExperiment.getId());
        assertEquals(2, nextRun.getId());
        assertEquals(2, nextResult.getId());
    }

    @Test
    //Проверка на коректность JSON
    void shouldNotReplaceCurrentDataWhenJsonIsInvalid() throws Exception {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var authService = new AuthService();
        var dataManager = new DataManager(experimentService, runService, resultService, authService);

        var existingExperiment = experimentService.add("Existing", "desc", 1);

        Path file = tempDir.resolve("bad.json");
        Files.writeString(file, """
                {
                  "experiments": [],
                  "runs": [
                    {
                      "id": 1,
                      "experimentId": 999,
                      "name": "Broken run",
                      "operatorName": "operator",
                      "createdAt": "2026-04-21T10:00:00Z",
                      "updatedAt": "2026-04-21T10:00:00Z"
                    }
                  ],
                  "runResults": []
                }
                """);

        assertThrows(ValidationException.class, () -> dataManager.loadFromFile(file.toString()));
        assertEquals(1, experimentService.list().size());
        assertEquals("Existing", experimentService.getById(existingExperiment.getId()).getName());
        assertTrue(runService.list().isEmpty());
        assertTrue(resultService.list().isEmpty());
    }
}
