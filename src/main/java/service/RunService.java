package service;

import database.RunRepository;
import domain.Run;
import validation.ValidationException;

import java.util.*;

public class RunService {
    //    Ключ - id прогона
    private final TreeMap<Long, Run> runs = new TreeMap<>();
    //    Создаём для проверки того, что прогон создается только для существующего эксперимента
    private final ExperimentService experimentService;
    //    Локальный счётчик ID, генерация происходит в сервисе
    private long nextId = 1;
    private final RunRepository runRepository;

//    старый режим - только TreeMap
    public RunService(ExperimentService experimentService) {
        this(experimentService, null);
    }

//    новый режим - подключение к БД через RunRepository
    public RunService(ExperimentService experimentService, RunRepository runRepository) {
        this.experimentService = experimentService;
        this.runRepository = runRepository;
        if (runRepository != null) {
            loadRestored(runRepository.findAll());
        }
    }

    private long generateNextId() {
        return nextId++;
    }

    public Run add(long experimentId, String name, String operatorName) {
//        Перед добавлением проверяем, что "родительский" Experiment существует
        experimentService.getById(experimentId);
        validateRunData(experimentId, name, operatorName);

        Run run;
        if (runRepository != null) {
//            если БД подключена, то генерация id происходит в ней
            run = runRepository.insert(experimentId, name, operatorName);
            nextId = Math.max(nextId, run.getId() + 1);
        } else {
            long id = generateNextId();
            run = new Run(id, experimentId, name, operatorName);
        }
        runs.put(run.getId(), run);
        return run;
    }

    private void validateRunData(long experimentId, String name, String operatorName) {
        new Run(1, experimentId, name, operatorName);
    }

    public void remove(long id) {
        if (!runs.containsKey(id)) {
//            если прогона с таким номером НЕТ - кидаем исключение
            throw new ValidationException("Run with id " + id + " doesn't exist");
        }
        if (runRepository != null) {
            runRepository.delete(id);
        }
        runs.remove(id);
    }

    public Run update(long id, String name, String operatorName) {
//        Обновление реализуется доменным объектом, а репозиторий сохраняет новое состояние в БД
        Run run = getById(id);
        run.update(name, operatorName);
        if (runRepository != null) {
            runRepository.update(run);
        }
        return run;
    }

    public Run getById(long id) {
        Run run = runs.get(id);

        if (run == null) {
            throw new ValidationException("Run with id " + id + " doesn't exist");
        }
        return run;
    }

    public Collection<Run> list() {
        return List.copyOf(runs.values()); // возвращаем копию, чтобы никак нельзя было извне поменять оригинал
    }

    public Collection<Run> listByExpId(long experimentId) {
//        Если ExperimentId не существует, будем считать это ошибкой, а не пустым результатом
        experimentService.getById(experimentId);

//        Берём все Run и оставляем только те, что относятся к нужному нам Experiment
        return runs.values().stream()
                .filter(run -> run.getExperimentId() == experimentId)
                .toList();
    }

    // Возвращаем копию коллекции для сохранения
    public List<Run> snapshot() {
        return new ArrayList<>(runs.values());
    }

    public void refreshFromRepository() {
        if (runRepository == null) {
            return;
        }

        loadRestored(runRepository.findAll());
    }


    // Метод загружает восстановленные объекты и обновляет nextId
    public void loadRestored(List<Run> restoredRuns) {
        //Создаем временное хранилище куда будем складывать загруженные  прогоны
        Map<Long, Run> loadedRuns = new TreeMap<>();
        long maxId = 0;

        //Проходим по всем прогонам и проверяем что ссылаемся на существующий эксперемент
        for (Run run : restoredRuns) {
            experimentService.getById(run.getExperimentId());

            if (loadedRuns.put(run.getId(), run) != null) {//Если ID дублируется ошибка
                throw new ValidationException("Duplicate run id: " + run.getId());
            }

            maxId = Math.max(maxId, run.getId());//Обновляем max ID
        }
//Очищаем коллецию сервиса и загружаем новые данные с правильным ID
        runs.clear();
        runs.putAll(loadedRuns);
        nextId = maxId + 1;
    }
}
