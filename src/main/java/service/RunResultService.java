package service;

import database.RunResultRepository;
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
    private RunResultRepository runResultRepository;

//    старый режим без БД - через TreeMap
    public RunResultService(RunService runService) {
        this(runService, null);
    }

//    новый режим через БД, подключение через RunResultRepository
    public RunResultService(RunService runService, RunResultRepository runResultRepository) {
        this.runService = runService;
        this.runResultRepository = runResultRepository;
        if (runResultRepository != null) {
            loadRestored(runResultRepository.findAll());
        }
    }

    private long generateNextId() {
        return nextId++;
    }

    public RunResult add(long runId, MeasurementParam param, double value, String unit, String comment) {
//        Проверка существования "родительского" Run перед добавлением его результата
        runService.getById(runId);
        validateRunResultData(runId, param, value, unit, comment);

        RunResult result;
        if (runResultRepository != null) {
//            если БД подключена то генерация id происходит в ней
            result = runResultRepository.insert(runId, param, value, unit, comment);
            nextId = Math.max(nextId, result.getId() + 1);
        } else {
            long id = generateNextId();
            result = new RunResult(id, runId, param, value, unit, comment);
        }
        results.put(result.getId(), result);
        return result;
    }

    private void validateRunResultData(long runId, MeasurementParam param, double value, String unit, String comment) {
        new RunResult(1, runId, param, value, unit, comment);
    }

    public void remove(long id) {
        if (!results.containsKey(id)) {
//            если результата с таким номером нет - исключение
            throw new ValidationException("RunResult with id " + id + " not found");
        }
        if (runResultRepository != null) {
            runResultRepository.delete(id);
        }
        results.remove(id);
    }

    public RunResult update(long id, MeasurementParam param, double value, String unit, String comment) {
//        Сервис находит нужный объект по ID, обновление реализуется доменным объектом, репозиторий сохраняет новое состояние в БД
        RunResult result = getById(id);
        result.update(param, value, unit, comment);
        if (runResultRepository != null) {
            runResultRepository.update(result);
        }
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

    // Возвращаем копию коллекции для сохранения
    public List<RunResult> snapshot() {
        return new ArrayList<>(results.values());
    }

    public void refreshFromRepository() {
        if (runResultRepository == null) {
            return;
        }

        loadRestored(runResultRepository.findAll());
    }


    // Метод загружает восстановленные объекты и обновляет nextId
    public void loadRestored(List<RunResult> restoredResults) {
        //Создаем временное хранилище куда будем складывать загруженные результаты прогона
        Map<Long, RunResult> loadedResults = new TreeMap<>();
        long maxId = 0;

        //Проходим по всем результатам прогонов и проверяем что ссылаемся на существующий прогон
        for (RunResult result : restoredResults) {
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

