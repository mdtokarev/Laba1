package storage;

import domain.Experiment;
import domain.MeasurementParam;
import domain.Run;
import domain.RunResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SnapshotMapperTest {
    private final SnapshotMapper mapper = new SnapshotMapper();

    @Test
    //Проверка что mapper умеет правильно переводить обычные объкты в структуру, удобную для JSON, данные не теряются, связи ID не ломаются, нужные поля правильно копируются
    void shouldConvertDomainObjectsToSnapshot() {
        var experiment = new Experiment(1, "Exp", "desc", 1);
        var run = new Run(1, experiment.getId(), "Run", "operator");
        var result = new RunResult(1, run.getId(), MeasurementParam.pH, 7.0, "pH", "ok");

        DataSnapshot snapshot = mapper.toSnapshot(
                List.of(experiment),
                List.of(run),
                List.of(result)
        );

        assertEquals("Exp", snapshot.getExperiments().get(0).getName());
        assertEquals(experiment.getId(), snapshot.getRuns().get(0).getExperimentId());
        assertEquals("pH", snapshot.getRunResults().get(0).getParam());
    }

    @Test
    //Проверить, что mapper умеет корректно восстанавливать domain объекты из snapshot
    void shouldRestoreDomainObjectsFromSnapshotWithTime() {
        var createdAt = Instant.parse("2026-04-21T10:00:00Z");
        var updatedAt = Instant.parse("2026-04-21T10:05:00Z");

        DataSnapshot snapshot = new DataSnapshot(
                List.of(new ExperimentData(1L, "Exp", "desc", 1L,
                        createdAt.toString(), updatedAt.toString())),
                List.of(new RunData(1L, 1L, "Run", "operator",
                        createdAt.toString(), updatedAt.toString())),
                List.of(new RunResultData(1L, 1L, "pH", 7.0, "pH", "ok",
                        createdAt.toString(), updatedAt.toString()))
        );

        Experiment experiment = mapper.toExperiments(snapshot).get(0);
        Run run = mapper.toRuns(snapshot).get(0);
        RunResult result = mapper.toRunResults(snapshot).get(0);

        assertEquals(1, experiment.getId());
        assertEquals(createdAt, experiment.getCreatedAt());
        assertEquals(updatedAt, experiment.getUpdatedAt());
        assertEquals(1, run.getExperimentId());
        assertEquals(createdAt, run.getCreatedAt());
        assertEquals(updatedAt, run.getUpdatedAt());
        assertEquals(MeasurementParam.pH, result.getParam());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(updatedAt, result.getUpdatedAt());
    }
}
