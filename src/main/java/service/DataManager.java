package service;

import domain.Experiment;
import domain.Run;
import domain.RunResult;
import domain.User;
import storage.DataSnapshot;
import storage.FileDataAccess;
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
    private final AuthService authService;

    private final FileDataAccess fileDataAccess;
    private final SnapshotMapper mapper;
    private final SnapshotValidator validator;

    public DataManager(ExperimentService experimentService, RunService runService, RunResultService resultService, AuthService authService) {
        this.experimentService = experimentService;
        this.runService = runService;
        this.resultService = resultService;
        this.authService = authService;
        this.fileDataAccess = new FileDataAccess();
        this.mapper = new SnapshotMapper();
        this.validator = new SnapshotValidator();
    }

    //Метод для сохранения в файл
    public void saveToFile(String path) throws IOException {
        //Собираем все данные в DataSnapshot
        DataSnapshot snapshot = mapper.toSnapshot(experimentService.snapshot(), runService.snapshot(), resultService.snapshot());
       //Переводим данные в формат для JSON
        fileDataAccess.saveData(Path.of(path), snapshot);
    }

    //Метод сохранения данных из файла
    public void loadFromFile(String path) throws IOException {
        //Читаем JSON-файл и получаем DataSnapshot
        DataSnapshot snapshot = fileDataAccess.loadData(Path.of(path));
        validator.validate(snapshot);//Валидируем их

        //mapper восстанавливает обычные domain объекты
        List<Experiment> experiments = mapper.toExperiments(snapshot);
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

    //Сохранение пользователя в файл
    public void saveUsersToFile(String path) throws IOException {
        //Берем текущий список пользователей и записываем в файл
        fileDataAccess.saveUsers(Path.of(path), authService.snapshot());
    }

    //Метод заргужает пользователей из файла
    public void loadUsersFromFile(String path) throws IOException {
        //Читаем список пользователей из файла
        List<User> users = fileDataAccess.loadUsers(Path.of(path));
        //Передаем пользователей в коллекцию
        authService.loadRestored(users);
    }
}