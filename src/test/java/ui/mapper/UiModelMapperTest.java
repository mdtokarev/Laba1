package ui.mapper;

import domain.Experiment;
import domain.MeasurementParam;
import domain.Run;
import domain.RunResult;
import org.junit.jupiter.api.Test;
import service.ParamStatistics;
import ui.viewmodel.ExperimentRow;
import ui.viewmodel.RunResultRow;
import ui.viewmodel.RunRow;
import ui.viewmodel.SummaryRow;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UiModelMapperTest {
    private final UiModelMapper mapper = new UiModelMapper();

    @Test
    void toExperimentRowCopiesExperimentFields() {
        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T10:00:00Z");
        Experiment experiment = Experiment.restore(1, "Experiment", "Description", "owner", createdAt, updatedAt);

        ExperimentRow row = mapper.toExperimentRow(experiment);

        assertEquals(1, row.getId());
        assertEquals("Experiment", row.getName());
        assertEquals("Description", row.getDescription());
        assertEquals("owner", row.getOwnerUsername());

//        проверяем что даты перевелись в строку
        assertEquals(createdAt.toString(), row.getCreatedAt());
        assertEquals(updatedAt.toString(), row.getUpdatedAt());
    }

    @Test
    void toRunRowCopiesRunFields() {
        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T10:00:00Z");
        Run run = Run.restore(2, 1, "Run", "operator", createdAt, updatedAt);

        RunRow row = mapper.toRunRow(run);

        assertEquals(2, row.getId());
        assertEquals(1, row.getExperimentId());
        assertEquals("Run", row.getName());
        assertEquals("operator", row.getOperatorName());
        assertEquals(createdAt.toString(), row.getCreatedAt());
        assertEquals(updatedAt.toString(), row.getUpdatedAt());
    }

    @Test
    void toRunResultRowCopiesResultFields() {
        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-02T10:00:00Z");
        RunResult result = RunResult.restore(3, 2, MeasurementParam.pH, 7.0, "pH", "ok", createdAt, updatedAt);

        RunResultRow row = mapper.toRunResultRow(result);

        assertEquals(3, row.getId());
        assertEquals(2, row.getRunId());
        assertEquals("pH", row.getParam());
        assertEquals(7.0, row.getValue());
        assertEquals("pH", row.getUnit());
        assertEquals("ok", row.getComment());
        assertEquals(createdAt.toString(), row.getCreatedAt());
        assertEquals(updatedAt.toString(), row.getUpdatedAt());
    }

    @Test
    void toSummaryRowCopiesStatisticsFields() {
        ParamStatistics statistics = new ParamStatistics(MeasurementParam.Temperature);
        statistics.addValue(20.0);
        statistics.addValue(24.0);

        SummaryRow row = mapper.toSummaryRow(statistics);

        assertEquals("Temperature", row.getParam());
        assertEquals(2, row.getCount());
        assertEquals(20.0, row.getMin());
        assertEquals(24.0, row.getMax());
        assertEquals(22.0, row.getAverage());
    }
}
