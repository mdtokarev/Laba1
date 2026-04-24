package service;

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

    public RunService(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    private long generateNextId() {
        return nextId++;
    }

    public Run add(long experimentId, String name, String operatorName) {
//        Перед добавлением проверяем, что "родительский" Experiment существует
        experimentService.getById(experimentId);

        long id = generateNextId();
        Run run = new Run(id, experimentId, name, operatorName);
        runs.put(id, run);
        return run;
    }

    public void remove(long id) {
        if (!runs.containsKey(id)) {
            throw new ValidationException("Run with id " + id + " doesn't exist");
        }
//        если эксперимента с таким номером НЕТ - кидаем исключение
        runs.remove(id);
    }

    public Run update(long id, String name, String operatorName) {
//        Обновление реализуется доменным объектом
        Run run = getById(id);
        run.update(name, operatorName);
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
    // 3 ЭТАП: JSON
    // Возвращаем копию коллекции для сохранения
    public List<Run> snapshot() {
        return new ArrayList<>(runs.values());
    }

    // 3 ЭТАП: JSON
    // Метод загружает восстановленные объекты и обновляет nextId
    public void loadRestored(List<Run> restoredRuns) {
        Map<Long, Run> loadedRuns = new TreeMap<>();//Создаем временное хранилище куда будем складывать загруженные  прогоны
        long maxId = 0;

        for (Run run : restoredRuns) {//Проходим по всем прогонам и проверяем что ссылаемся на существующий эксперемент
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
