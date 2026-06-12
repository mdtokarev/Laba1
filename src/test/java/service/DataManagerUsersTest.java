package service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DataManagerUsersTest {

    @TempDir
    Path tempDir;

    @Test
    // Проверяем что пользователи сохраняются и загружаются отдельно
    void shouldSaveAndLoadUsers() throws Exception {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var authService = new AuthService();
        var dataManager = new DataManager(experimentService, runService, resultService, authService);

        authService.register("user1", "password");

        Path file = tempDir.resolve("users.json");
        dataManager.saveUsersToFile(file.toString());

        var loadedExperimentService = new ExperimentService();
        var loadedRunService = new RunService(loadedExperimentService);
        var loadedResultService = new RunResultService(loadedRunService);
        var loadedAuthService = new AuthService();
        var loadedDataManager = new DataManager(loadedExperimentService, loadedRunService, loadedResultService, loadedAuthService);

        loadedDataManager.loadUsersFromFile(file.toString());
        var loadedUser = loadedAuthService.login("user1", "password");

        assertEquals(1, loadedUser.getId());
        assertEquals("user1", loadedUser.getLogin());
    }
}
