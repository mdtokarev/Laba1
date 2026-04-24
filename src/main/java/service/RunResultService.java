package service;

import domain.MeasurementParam;
import domain.RunResult;
import validation.ValidationException;

import java.util.*;

public class RunResultService {

    private final TreeMap<Long, RunResult> results = new TreeMap<>();
    //    Нужно для проверки, что результат создаётся только для существующего Run
    private final RunService runService;
    //     Локальный счётчик ID, генерируется в сервисе
    private long nextId = 1;

    public RunResultService(RunService runService) {
        this.runService = runService;
    }

    private long generateNextId() {
        return nextId++;
    }

    public RunResult add(long runId, MeasurementParam param, double value, String unit, String comment) {
//        Проверка существования "родительского" Run перед добавлением его результата
        runService.getById(runId);

        long id = generateNextId();
        RunResult result = new RunResult(id, runId, param, value, unit, comment);
        results.put(id, result);
        return result;
    }

    public void remove(long id) {
        if (!results.containsKey(id)) {
            throw new ValidationException("RunResult with id " + id + " not found");
        }
        results.remove(id);
    }

    public RunResult update(long id, MeasurementParam param, double value, String unit, String comment) {
//        Сервис находит нужный объект по ID, обновление реализуется доменным объектом
        RunResult result = getById(id);
        result.update(param, value, unit, comment);
        return result;
    }

    public RunResult getById(long id) {
        RunResult result = results.get(id);

        if (result == null) {
            throw new ValidationException("RunResult with id " + id + " not found");
        }
        return result;
    }

    public Collection<RunResult> list() {
        return List.copyOf(results.values());
    }

    public Collection<RunResult> listByRunId(long runId) {
//        Сначала убеждаемся, что такой Run существует
        runService.getById(runId);
//        Фильтруем все результаты и оставляем рез-ты только указанного прогона
        return results.values().stream()
                .filter(result -> result.getRunId() == runId)
                .toList();
    }
    // 3 ЭТАП: JSON
    // Возвращаем копию коллекции для сохранения
    public List<RunResult> snapshot() {
        return new ArrayList<>(results.values());
    }

    // 3 ЭТАП: JSON
    // Метод загружает восстановленные объекты и обновляет nextId
    public void loadRestored(List<RunResult> restoredResults) {
        Map<Long, RunResult> loadedResults = new TreeMap<>();//Создаем временное хранилище куда будем складывать загруженные результаты прогона
        long maxId = 0;

        for (RunResult result : restoredResults) {//Проходим по всем результатам прогонов и проверяем что ссылаемся на существующий прогон
            runService.getById(result.getRunId());

            if (loadedResults.put(result.getId(), result) != null) {//Если уже что то лежало под таким ID ошибка
                throw new ValidationException("Duplicate run result id: " + result.getId());
            }

            maxId = Math.max(maxId, result.getId());//Обновляем max ID
        }
//Очищаем коллецию сервиса и загружаем новые данные с правильным ID
        results.clear();
        results.putAll(loadedResults);
        nextId = maxId + 1;
    }
}

