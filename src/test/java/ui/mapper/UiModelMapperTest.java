package ui.mapper;

import domain.Experiment;
import domain.MeasurementParam;
import domain.Run;
import domain.RunResult;
import org.junit.jupiter.api.Test;
import service.ParamStatistics;

import static org.junit.jupiter.api.Assertions.*;

class UiModelMapperTest {

    @Test
    //Проверка что Experiment правильно превращается в ExperimentRow
    void shouldConvertExperimentToRow() {
        var mapper = new UiModelMapper();
        var experiment = new Experiment(1, "Exp", "desc", 1);

        var row = mapper.toExperimentRow(experiment);

        assertEquals(1, row.getId());
        assertEquals("Exp", row.getName());
        assertEquals("desc", row.getDescription());
        assertEquals(1, row.getOwnerId());
        assertNotNull(row.getCreatedAt());
        assertNotNull(row.getUpdatedAt());
    }

    @Test
    //Проверка что Run правильно превращается в RunRow
    void shouldConvertRunToRow() {
        var mapper = new UiModelMapper();
        var run = new Run(1, 2, "Run", "operator");

        var row = mapper.toRunRow(run);

        assertEquals(1, row.getId());
        assertEquals(2, row.getExperimentId());
        assertEquals("Run", row.getName());
        assertEquals("operator", row.getOperatorName());
    }

    @Test
    //Проверка что RunResult правильно превращается в RunResultRow
    void shouldConvertRunResultToRow() {
        var mapper = new UiModelMapper();
        var result = new RunResult(1, 2, MeasurementParam.pH, 7.0, "pH", "ok");

        var row = mapper.toRunResultRow(result);

        assertEquals(1, row.getId());
        assertEquals(2, row.getRunId());
        assertEquals("pH", row.getParam());
        assertEquals(7.0, row.getValue());
        assertEquals("pH", row.getUnit());
        assertEquals("ok", row.getComment());
    }

    @Test
    //Проверка что ParamStatistics правильно превращается в SummaryRow
    void shouldConvertStatisticsToRow() {
        var mapper = new UiModelMapper();
        var statistics = new ParamStatistics(MeasurementParam.pH);
        statistics.addValue(6.0);
        statistics.addValue(8.0);

        var row = mapper.toSummaryRow(statistics);

        assertEquals("pH", row.getParam());
        assertEquals(2, row.getCount());
        assertEquals(6.0, row.getMin());
        assertEquals(8.0, row.getMax());
        assertEquals(7.0, row.getAverage());
    }
}
