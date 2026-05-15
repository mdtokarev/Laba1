package ui.mapper;

import domain.Experiment;
import domain.Run;
import domain.RunResult;
import service.ParamStatistics;
import ui.viewmodel.ExperimentRow;
import ui.viewmodel.RunResultRow;
import ui.viewmodel.RunRow;
import ui.viewmodel.SummaryRow;

//Класс для перевода доменами и UI
public class UiModelMapper {

    //Получаем эксперемнт и превращаем в ExperimentRow, особое внимание времени переводим в строку
    public ExperimentRow toExperimentRow(Experiment experiment) {
        return new ExperimentRow(experiment.getId(), experiment.getName(), experiment.getDescription(), experiment.getOwnerId(), experiment.getCreatedAt().toString(), experiment.getUpdatedAt().toString());
    }

    //Получаем прогон и превращаем в RunRow, особое внимание времени переводим в строку
    public RunRow toRunRow(Run run) {
        return new RunRow(run.getId(), run.getExperimentId(), run.getName(), run.getOperatorName(), run.getCreatedAt().toString(), run.getUpdatedAt().toString());
    }

    //Получаем результат прогона и превращаем в RunResultRow, особое внимание времени переводим в строку
    public RunResultRow toRunResultRow(RunResult result) {
        return new RunResultRow(result.getId(), result.getRunId(), result.getParam().name(), result.getValue(), result.getUnit(),result.getComment(), result.getCreatedAt().toString(), result.getUpdatedAt().toString());
    }

    //Получаем статистику и превращаем в SummaryRow
    public SummaryRow toSummaryRow(ParamStatistics statistics) {
        return new SummaryRow(statistics.getParam().name(), statistics.getCount(), statistics.getMin(), statistics.getMax(), statistics.getAverage());
    }
}
