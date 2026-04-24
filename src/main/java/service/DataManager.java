package service;

import domain.Experiment;
import domain.Run;
import domain.RunResult;
import storage.DataSnapshot;
import storage.JsonFileStorage;
import storage.SnapshotMapper;
import storage.SnapshotValidator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

// Главный класс для сохранения и загрузки
public class DataManager {
    private final ExperimentService experimentService;
    private final RunService runService;
    private final RunResultService resultService;

    private final JsonFileStorage storage;
    private final SnapshotMapper mapper;
    private final SnapshotValidator validator;

    public DataManager(ExperimentService experimentService, RunService runService, RunResultService resultService) {
        this.experimentService = experimentService;
        this.runService = runService;
        this.resultService = resultService;
        this.storage = new JsonFileStorage();
        this.mapper = new SnapshotMapper();
        this.validator = new SnapshotValidator();
    }

    //Метод для сохранения в файл
    public void saveToFile(String path) throws IOException {
        DataSnapshot snapshot = mapper.toSnapshot(experimentService.snapshot(), runService.snapshot(), resultService.snapshot());//Собираем все данные в DataSnapshot
//Переводим данные в формат для JSON
        storage.save(Path.of(path), snapshot);
    }

    //Метод сохранения данных из файла
    public void loadFromFile(String path) throws IOException {
        DataSnapshot snapshot = storage.load(Path.of(path));//Читаем JSON-файл и получаем DataSnapshot
        validator.validate(snapshot);//Валидируем их

        List<Experiment> experiments = mapper.toExperiments(snapshot);//mapper восстанавливает обычные domain объекты
        List<Run> runs = mapper.toRuns(snapshot);
        List<RunResult> results = mapper.toRunResults(snapshot);

        //Создаем временные сервисы, чтобы привести данные в порядок
        ExperimentService tempExperimentService = new ExperimentService();
        RunService tempRunService = new RunService(tempExperimentService);
        RunResultService tempResultService = new RunResultService(tempRunService);

        //Загружаем данные во временные сервисы
        tempExperimentService.loadRestored(experiments);
        tempRunService.loadRestored(runs);
        tempResultService.loadRestored(results);

        //Если все хорошо переносим в основные сервисы
        experimentService.loadRestored(tempExperimentService.snapshot());
        runService.loadRestored(tempRunService.snapshot());
        resultService.loadRestored(tempResultService.snapshot());
    }
}
