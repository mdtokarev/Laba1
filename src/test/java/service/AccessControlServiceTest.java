package service;

import domain.MeasurementParam;
import org.junit.jupiter.api.Test;
import validation.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

class AccessControlServiceTest {

    @Test
    // Проверяем что владелец может менять свои данные
    void shouldAllowOwnerToModifyObjects() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var accessControlService = new AccessControlService(experimentService, runService, resultService);

        var experiment = experimentService.add("Exp", "desc", 1);
        var run = runService.add(experiment.getId(), "Run", "operator");
        var result = resultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", null);

        assertDoesNotThrow(() -> accessControlService.checkCanModifyExperiment(1, experiment.getId()));
        assertDoesNotThrow(() -> accessControlService.checkCanModifyRun(1, run.getId()));
        assertDoesNotThrow(() -> accessControlService.checkCanModifyResult(1, result.getId()));
    }

    @Test
    // Проверяем что чужой пользователь не может менять данные владельца
    void shouldDenyOtherUserAccess() {
        var experimentService = new ExperimentService();
        var runService = new RunService(experimentService);
        var resultService = new RunResultService(runService);
        var accessControlService = new AccessControlService(experimentService, runService, resultService);

        var experiment = experimentService.add("Exp", "desc", 1);
        var run = runService.add(experiment.getId(), "Run", "operator");
        var result = resultService.add(run.getId(), MeasurementParam.pH, 7.0, "pH", null);

        assertThrows(ValidationException.class, () ->
                accessControlService.checkCanModifyExperiment(2, experiment.getId()));
        assertThrows(ValidationException.class, () ->
                accessControlService.checkCanModifyRun(2, run.getId()));
        assertThrows(ValidationException.class, () ->
                accessControlService.checkCanModifyResult(2, result.getId()));
    }
}
