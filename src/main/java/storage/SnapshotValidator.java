package storage;

import domain.MeasurementParam;
import validation.ValidationException;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Проверяет данные из JSON до загрузки в сервисы.
public class SnapshotValidator {

    //Проверяет что snapshot не пустой
    public void validate(DataSnapshot snapshot) {
        if (snapshot == null) {
            throw new ValidationException("Snapshot can't be null");
        }

        //Достаем данные и проверяем их существование
        List<ExperimentData> experiments = requireSection(snapshot.getExperiments(), "experiments");
        List<RunData> runs = requireSection(snapshot.getRuns(), "runs");
        List<RunResultData> results = requireSection(snapshot.getRunResults(), "runResults");


        //Прроверяем наши объекты и связь между ними
        validateExperiments(experiments);
        validateRuns(runs);
        validateResults(results);
        validateLinks(experiments, runs, results);
    }

    //Проверяем существует ли секция вообще
    private <T> List<T> requireSection(List<T> section, String name) {
        if (section == null) {
            throw new ValidationException("File is missing '" + name + "' section");
        }
        return section;
    }

    //
    private void validateExperiments(List<ExperimentData> experiments) {
        Set<Long> ids = new HashSet<>();//Создаем Set, чтобы запоминать уже встреченные ID

        //Проходим по каждому эксперементу и проверяем что не null
        for (ExperimentData experiment : experiments) {
            requireNotNull(experiment, "experiments");

            //Проверяем что ID положительный, если все хорошо сохраняем и если ID повторяются выкидываем ошибку
            long id = requirePositive(experiment.getId(), "Experiment.id");
            requireUnique(ids, id, "Duplicate experiment id: ");

            //Проверяем что имя не пустое или выкидываем ошибку
            requireNonBlank(experiment.getName(), "Experiment.name can't be empty");
            //Проверяем что имя не длинее 128 или выкидываем ошибку
            requireMaxLength(experiment.getName(), 128, "Experiment.name too long");
            //Провеярем описание не длинее 512
            requireMaxLength(experiment.getDescription(), 512, "Experiment.description too long");
            //Провеярем что укзан владелец
            requireNonBlank(experiment.getOwnerUsername(), "Experiment.ownerUsername can't be empty");
            //Длина имени не больше 128
            requireMaxLength(experiment.getOwnerUsername(), 128, "Experiment.ownerUsername too long");
            //Проверяем коректность дат
            validateDates(experiment.getCreatedAt(), experiment.getUpdatedAt(), "Experiment id=" + id);
        }
    }

    private void validateRuns(List<RunData> runs) {
        Set<Long> ids = new HashSet<>();//Создаем Set, чтобы запоминать уже встреченные ID

        //Проходим по каждому прогону и проверяем что не null
        for (RunData run : runs) {
            requireNotNull(run, "runs");

            //Проверяем что ID положительный, если все хорошо сохраняем и если ID повторяются выкидываем ошибку
            long id = requirePositive(run.getId(), "Run.id");
            requireUnique(ids, id, "Duplicate run id: ");

            //Проверяем,что experimentId указан и положительный
            requirePositive(run.getExperimentId(), "Run.experimentId");
            //Имя запуска не null
            requireNonBlank(run.getName(), "Run.name can't be empty");
            //Имя запуска не больше 128
            requireMaxLength(run.getName(), 128, "Run.name too long");
            //Владелец должен быть указан
            requireNonBlank(run.getOperatorName(), "Run.operatorName can't be empty");
            //Имя владельца не больше 64
            requireMaxLength(run.getOperatorName(), 64, "Run.operatorName too long");
            //Коректность времени
            validateDates(run.getCreatedAt(), run.getUpdatedAt(), "Run id=" + id);
        }
    }

    private void validateResults(List<RunResultData> results) {
        Set<Long> ids = new HashSet<>();//Создаем Set, чтобы запоминать уже встреченные ID

        //Проходим по каждому результату прогона и проверяем что не null
        for (RunResultData result : results) {
            requireNotNull(result, "runResults");

            //Проверяем что ID положительный, если все хорошо сохраняем и если ID повторяются выкидываем ошибку
            long id = requirePositive(result.getId(), "RunResult.id");
            requireUnique(ids, id, "Duplicate run result id: ");

            //Проверяем что runId есть и он положительный
            requirePositive(result.getRunId(), "RunResult.runId");
            //Превращаем строку параметра в енум
            MeasurementParam param = parseParam(result.getParam());
            //Проверяем наличие измерений
            if (result.getValue() == null) {
                throw new ValidationException("RunResult.value is required");
            }
            //Проверяем коректность параметров
            validateValue(param, result.getValue());

            //Проверяем наличие едениц измерения
            requireNonBlank(result.getUnit(), "RunResult.unit can't be empty");
            //Еденица измерения не длиньше 16
            requireMaxLength(result.getUnit(), 16, "RunResult.unit too long");
            //Длина описания не ьольше 128
            requireMaxLength(result.getComment(), 128, "RunResult.comment too long");
            //Коректность дат
            validateDates(result.getCreatedAt(), result.getUpdatedAt(), "RunResult id=" + id);
        }
    }

    private void validateLinks(List<ExperimentData> experiments, List<RunData> runs, List<RunResultData> results) {
        Set<Long> experimentIds = new HashSet<>();//Создаем Set ID, обираем все ID экспериментов в Set
        for (ExperimentData experiment : experiments) {
            experimentIds.add(experiment.getId());
        }

        Set<Long> runIds = new HashSet<>();//Создаем Set ID, проходим по запускам, проверяем существует ли эксперимент, на который ссылается запуск, если нет ошибка
        for (RunData run : runs) {
            if (!experimentIds.contains(run.getExperimentId())) {
                throw new ValidationException("Run id=" + run.getId() + " references missing experiment id=" + run.getExperimentId());
            }
            runIds.add(run.getId());
        }
//Проходим по результатам запусков, проверяем существует ли запуск, на который ссылается результат, если нет ошибка
        for (RunResultData result : results) {
            if (!runIds.contains(result.getRunId())) {
                throw new ValidationException("RunResult id=" + result.getId() + " references missing run id=" + result.getRunId());
            }
        }
    }

    //Проверка что объект не пуст
    private void requireNotNull(Object object, String section) {
        if (object == null) {
            throw new ValidationException("Section '" + section + "' contains null item");
        }
    }

    //Проверка что число есть и оно положительное
    private long requirePositive(Long value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " is required");
        }
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be positive");
        }
        return value;
    }

    //Проерка уникальности ID
    private void requireUnique(Set<Long> ids, long id, String message) {
        if (!ids.add(id)) {
            throw new ValidationException(message + id);
        }
    }

    //Проверка на пустоту или только из пробелов
    private void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
    }

    //Проверка длины строки
    private void requireMaxLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new ValidationException(message);
        }
    }

    //Проверка параметр должен быть быть указан
    private MeasurementParam parseParam(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("RunResult.param is required");
        }
      //Пробуем превратить строку в enum, если такого нет ошибка
        try {
            return MeasurementParam.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown MeasurementParam: " + value);
        }
    }

    //Проверка на рельность параметра
    private void validateValue(MeasurementParam param, double value) {
        switch (param) {
            case pH -> {
                if (value < 0 || value > 14) {
                    throw new ValidationException("pH must be between 0 and 14");
                }
            }
            case Concentration -> {
                if (value < 0) {
                    throw new ValidationException("Concentration can't be negative");
                }
            }
            case Temperature -> {
            }
        }
    }

    //Проверка коректности дат
    private void validateDates(String createdAt, String updatedAt, String label) {
        Instant created = parseInstant(createdAt, label + " has invalid createdAt");
        Instant updated = parseInstant(updatedAt, label + " has invalid updatedAt");

        if (updated.isBefore(created)) {
            throw new ValidationException(label + " has updatedAt before createdAt");
        }
    }

    //Преобразование строки даты в Instant
    private Instant parseInstant(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }

        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            throw new ValidationException(message);
        }
    }
}
