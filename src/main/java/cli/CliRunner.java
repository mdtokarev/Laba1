package cli;

import domain.Experiment;
import domain.MeasurementParam;
import domain.Run;
import domain.RunResult;
import service.DataManager;
import service.ExperimentService;
import service.RunResultService;
import service.RunService;
import validation.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

public class CliRunner {
    private final Scanner scanner;
    private final PrintStream out;

    private final ExperimentService experimentService;
    private final RunService runService;
    private final RunResultService runResultService;

    //  3 ЭТАП: JSON
    // DataManager отвечает за сохранение и загрузку всех коллекций
    private final DataManager dataManager;
    private String currentFilePath;

    private boolean running;

    public CliRunner() {
        this(System.in, System.out);
    }

    public CliRunner(InputStream inputStream, PrintStream out) {
        this(inputStream, out, null);
    }

    public CliRunner(InputStream inputStream, PrintStream out, String initialFilePath) {
        this.scanner = new Scanner(inputStream);
        this.out = out;

        this.experimentService = new ExperimentService();
        this.runService = new RunService(experimentService);
        this.runResultService = new RunResultService(runService);

        // ===== 3 ЭТАП: JSON =====
        this.dataManager = new DataManager(experimentService, runService, runResultService);
        this.currentFilePath = initialFilePath;

        // 3 ЭТАП: JSON
        // Если путь передали при запуске, программа пробует загрузить файл сразу
        if (initialFilePath != null && !initialFilePath.isBlank()) {
            try {
                dataManager.loadFromFile(initialFilePath);
                out.println("Data loaded from " + initialFilePath);
            } catch (IOException | ValidationException e) {
                out.println("Warning: could not load initial file: " + e.getMessage());
                out.println("Starting with empty collections.");
            }
        }
    }

    public static void run() {
        new CliRunner().start();
    }

    void start() {
        running = true;
        printWelcome();

//        hasNextLine() проверяет, не закончился ли ввод в консоль
        while (running && scanner.hasNextLine()) {
            out.print("> ");

//            .nextLine() читает след. строку до нажатия Enter
//            .trim() убирает пробелы в начале и конце строки - " help " -> "help"
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            handleCommand(line);
        }
    }

    private void handleCommand(String line) {
        ParsedCommand parsedCommand = parseCommand(line);

        try {
            switch (parsedCommand.name()) {
//                Перебираем команды и вызываем соответствующий метод
                case "help" -> printHelp();
                // 3 ЭТАП: JSON
                case "save" -> handleSave(parsedCommand);
                case "load" -> handleLoad(parsedCommand);
                case "exit" -> handleExit();
                case "exp_add" -> handleExperimentAdd(parsedCommand);
                case "exp_list" -> handleExperimentList(parsedCommand);
                case "exp_show" -> handleExperimentShow(parsedCommand);
                case "exp_update" -> handleExperimentUpdate(parsedCommand);
                case "run_add" -> handleRunAdd(parsedCommand);
                case "run_list" -> handleRunList(parsedCommand);
                case "run_show" -> handleRunShow(parsedCommand);
                case "res_add" -> handleResultAdd(parsedCommand);
                case "res_list" -> handleResultList(parsedCommand);
                case "exp_summary" -> handleExperimentSummary(parsedCommand);
                default -> out.println("Unknown command: " + line + ". Type 'help' to see available commands.");
            }
        } catch (ValidationException e) {
            out.println("Validation error: " + e.getMessage());
        } catch (RuntimeException e) {
            out.println("Unexpected error: " + e.getMessage());
        }
    }

    //    Реализация того, что CLI будет отличать ввод самой команды от передаваемых ей аргументов
    private ParsedCommand parseCommand(String line) {

//        Строка, введённая пользователем (line) разделится по любому пробельному символу
//        Максимум строка будет разделена на 2 части - имя команды + хвост из аргументов
        String[] parts = line.split("\\s+", 2);

//        Приводит всю строку к нижнему регистру.
//        Обеспечивает одинаковый формат данных независимо от ОС или страны пользователя
        String name = parts[0].toLowerCase(Locale.ROOT);

//        Если длина массива строк, полученного ранее, > 1, то применяем .trim() к "хвосту"
//        Иначе - вернуть пустую строку
        String arguments = parts.length > 1 ? parts[1].trim() : "";
        return new ParsedCommand(name, arguments);
    }

    private void printWelcome() {
//        Выводит приветственное сообщение
        out.println("Experiment CLI started.");
        out.println("Type 'help' to see available commands.");
    }

    private void printHelp() {
//        Выводит список доступных программ
        out.println("Available commands:");
        out.println("help - show available commands");
        out.println("exp_add - create a new experiment");
        out.println("exp_list - show all experiments");
        out.println("exp_show <id> - show one experiment");
        out.println("exp_update <id> field=value ... - update experiment");
        out.println("run_add <experimentId> - create a run for experiment");
        out.println("run_list <experimentId> - show runs for experiment");
        out.println("run_show <runId> - show one run");
        out.println("res_add <runId> - add a result for run");
        out.println("res_list <runId> [--param PARAM] - show results for run");
        out.println("exp_summary <id> - show summary for experiment");
        // 3 ЭТАП: JSON
        out.println("save <path> - save all data to JSON file");
        out.println("load <path> - load data from JSON file");
        out.println("exit - stop the program");
    }

    private void handleExit() {
//        Останавливает цикл while
        running = false;
        out.println("CLI stopped.");
    }


    //    Обеспечиваем вывод команды без ошибок, так как ее запуск происходит без ввода аргументов.
//    Кидает исключение, если введём команду с каким-то аргументом (типа exp_add test)
    private void guaranteeNoArguments(ParsedCommand parsedCommand, String commandName) {
        if (!parsedCommand.arguments().isEmpty()) {
            throw new ValidationException(commandName + " does not accept arguments");
        }
    }

    private String readRequiredValue(String label) {
//        Здесь required = поля, которые не могут быть пустыми
        while (true) {
            out.print(label + ": ");
//            label = какой текст показать пользователю перед вводом (name/description/...)

            if (!scanner.hasNextLine()) {
                running = false;
                throw new ValidationException("Input stream was closed");
            }

            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
//            Если пользователь оставил строку пустой, просит ввести значение заново
            out.println(label + " can't be empty.");
        }
    }

    private String readOptionalValue(String label) {
//        Здесь optional = поля, которые могут быть пустыми
        out.print(label + ": ");

        if (!scanner.hasNextLine()) {
            running = false;
            throw new ValidationException("Input stream was closed");
        }

        String value = scanner.nextLine().trim();

//        Если value пустая, вернуть null; иначе - вернуть value (= if-else)
        return value.isBlank() ? null : value;
    }

    //    Команда exp_add - добавить эксперимент
    private void handleExperimentAdd(ParsedCommand parsedCommand) {

//        Проверяем что команда вызывается без аргументов
        guaranteeNoArguments(parsedCommand, "exp_add");

        out.println("Creating a new experiment.");

//        По очереди спрашивает name, description, owner username
        String name = readRequiredValue("Name");
        String description = readOptionalValue("Description");
        String ownerUsername = readRequiredValue("Owner username");

//        Передаёт собранные данные в сервис, и выводит ID созданного эксперимента
        Experiment experiment = experimentService.add(name, description, ownerUsername);
        out.println("Experiment created with id " + experiment.getId());
    }

    private String formatExperimentLine(Experiment experiment) {

//        Метод получает объект эксперимента, и выводит текстовую строку из полей объекта
//        Если описание пустое, то выведет "-"
        String description = experiment.getDescription() == null ? "-" : experiment.getDescription();
        return experiment.getId()
                + " | "
                + " | name - " + experiment.getName()
                + " | owner - " + experiment.getOwnerUsername()
                + " | description - " + description;
    }

    //    Команда exp_list - показать список добавленных экспериментов
    private void handleExperimentList(ParsedCommand parsedCommand) {

//        Проверяем, что команда вызвана без аргументов
        guaranteeNoArguments(parsedCommand, "exp_list");

        var experiments = experimentService.list();
        if (experiments.isEmpty()) {
            out.println("No experiments found.");
            return;
        }

//        Если список не пустой, то форматированно выводит каждый эксперимент
        out.println("Experiments:");
        for (Experiment experiment : experiments) {
            out.println(formatExperimentLine(experiment));
        }
    }

    private long parseRequiredLongArgument(ParsedCommand parsedCommand, String commandName, String argumentLabel) {

//        Берёт строку аргументов
        String arguments = parsedCommand.arguments();
        if (arguments.isEmpty()) {
            throw new ValidationException(commandName + " requires " + argumentLabel);
        }

//        Делит эту строку по пробелам, если аргументов > 1 - исключение
        String[] parts = arguments.split("\\s+");
        if (parts.length != 1) {
            throw new ValidationException(commandName + " accepts exactly one argument: " + argumentLabel);
        }

//        Преобразование аргумента в long
        try {
            return Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
//            Ловим ввод типа "abc"
            throw new ValidationException(argumentLabel + " must be a number");
        }
    }

    private String formatNullableValue(String value) {
//        Если строковый value == null, то возвращает "-". Если нет - то само value
        return value == null ? "-" : value;
    }

    //    Команда exp_show - показать информацию по одному эксперименту
    private void handleExperimentShow(ParsedCommand parsedCommand) {

//        Вызывает метод parse..(), чтобы достать и валидировать ID
        long experimentId = parseRequiredLongArgument(parsedCommand, "exp_show", "experiment id");
        Experiment experiment = experimentService.getById(experimentId);

        out.println("Experiment details:");
        out.println("Id: " + experiment.getId());
        out.println("Name: " + experiment.getName());

//        Используем метод format..(), чтобы в случае пустого описания вывести "-"
        out.println("Description: " + formatNullableValue(experiment.getDescription()));
        out.println("Owner username: " + experiment.getOwnerUsername());
        out.println("Created at: " + experiment.getCreatedAt());
        out.println("Updated at: " + experiment.getUpdatedAt());
    }

    private ExperimentUpdateRequest parseExperimentUpdateRequest(ParsedCommand parsedCommand) {

//        Берёт всю строку аргументов после команды exp_update
        String arguments = parsedCommand.arguments();
        if (arguments.isEmpty()) {
            throw new ValidationException("exp_update requires experiment id and one field=value");
        }

//        Делит строку по пробелам на две части - id + field=value
        String[] parts = arguments.split("\\s+");
        long experimentId;
        try {
            experimentId = Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            throw new ValidationException("experiment id must be a number");
        }

        if (parts.length != 2) {
            throw new ValidationException("exp_update accepts exactly one field=value");
        }

//        Вторую часть делит на поле и значение
        String[] fieldAndValue = parts[1].split("=", 2);
        if (fieldAndValue.length != 2) {
            throw new ValidationException("Invalid update argument: " + parts[1]);
        }

        return new ExperimentUpdateRequest(experimentId, fieldAndValue[0], fieldAndValue[1]);
    }

    private void handleExperimentUpdate(ParsedCommand parsedCommand) {

//        Получает из парсера id, field, value, находит эксперимент по id
        ExperimentUpdateRequest request = parseExperimentUpdateRequest(parsedCommand);
        Experiment experiment = experimentService.getById(request.id());

        String updatedName = experiment.getName();
        String updatedDescription = experiment.getDescription();
        String updatedOwnerUsername = experiment.getOwnerUsername();

        switch (request.field()) {
//            За один цикл команды обновляем только одно поле
            case "name" -> updatedName = request.value();
            case "description" -> updatedDescription = request.value();
            case "ownerUsername" -> updatedOwnerUsername = request.value();
            default -> throw new ValidationException("Unknown experiment field: " + request.field());
        }

        experimentService.update(experiment.getId(), updatedName, updatedDescription, updatedOwnerUsername);
        out.println("Experiment updated.");
    }

    //    Команда run_add - добавить прогон эксперимента (интерактивно)
    private void handleRunAdd(ParsedCommand parsedCommand) {
//         Получение и проверка experimentId через парсер
        long experimentId = parseRequiredLongArgument(parsedCommand, "run_add", "experiment id");

        out.println("Creating a new run.");
//        Пользователь вводит параметры прогона
        String runName = readRequiredValue("Run name");
        String operatorName = readRequiredValue("Operator");

//        Передаёт в сервис полученные данные и выводит ID созданного run
        Run run = runService.add(experimentId, runName, operatorName);
        out.println("Run created with id " + run.getId());
    }

    //    Форматированный вывод списка прогонов
    private String formatRunLine(Run run) {
        return run.getId()
                + " | "
                + " | name - " + run.getName()
                + " | operator - " + run.getOperatorName();
    }

    //    Команда run_list - показать список прогонов
    private void handleRunList(ParsedCommand parsedCommand) {
//        Через парсер берет experimentId, получает эксперимент и его прогоны через сервисы
        long experimentId = parseRequiredLongArgument(parsedCommand, "run_list", "experiment id");
        Experiment experiment = experimentService.getById(experimentId);
//        Через сервис получает список всех прогонов этого эксперимента
        var runs = runService.listByExpId(experimentId);

        if (runs.isEmpty()) {
            out.println("No runs found for experiment " + experiment.getId() + ".");
            return;
        }

        out.println("Runs for experiment " + experiment.getId() + " (" + experiment.getName() + "):");
//        Если список прогонов не пустой, то для каждого форматированно выводит инфу
        for (Run run : runs) {
            out.println(formatRunLine(run));
        }
    }

    //    Команда run_show - показать информацию по одному run
    private void handleRunShow(ParsedCommand parsedCommand) {

//        Через парсер достаёт и валидирует id
        long runId = parseRequiredLongArgument(parsedCommand, "run_show", "run id");
        Run run = runService.getById(runId);
//        Через сервис получаем количество результатов данного прогона
        int resultCount = runResultService.listByRunId(runId).size();

        out.println("Run details:");
        out.println("Id: " + run.getId());
        out.println("Experiment id: " + run.getExperimentId());
        out.println("Name: " + run.getName());
        out.println("Operator: " + run.getOperatorName());
        out.println("Results: " + resultCount);
        out.println("Created at: " + run.getCreatedAt());
        out.println("Updated at: " + run.getUpdatedAt());
    }

    private double readRequiredDouble(String label) {
        while (true) {
            String rawValue = readRequiredValue(label);
            try {

//                Пытается превратить полученную строку в double
                return Double.parseDouble(rawValue);
            } catch (NumberFormatException e) {
                out.println(label + " must be a number.");
            }
        }
    }

    private MeasurementParam readMeasurementParam(String label) {
        while (true) {
            String rawValue = readRequiredValue(label);

//            Проходит по enum, сравнивает введённый текст с ним
//            Если есть совпадение - возвращает нужный param, если нет - просит ввести правильный
            for (MeasurementParam param : MeasurementParam.values()) {
                if (param.name().equalsIgnoreCase(rawValue)) {
                    return param;
                }
            }

            out.println(label + " must be one of: pH, Temperature, Concentration.");
        }
    }

    //    Команда res_add - добавить результат прогона
    private void handleResultAdd(ParsedCommand parsedCommand) {
//        Через парсер берёт id прогона
        long runId = parseRequiredLongArgument(parsedCommand, "res_add", "run id");

        out.println("Creating a new result.");
//        Интерактивный ввод параметров
        MeasurementParam param = readMeasurementParam("Parameter");
        double value = readRequiredDouble("Value");
        String unit = readRequiredValue("Unit");
        String comment = readOptionalValue("Comment");

//        Сервис добавляет результат в коллекцию, выводится id результата
        RunResult result = runResultService.add(runId, param, value, unit, comment);
        out.println("Result created with id " + result.getId());
    }

    private ResultListRequest parseResultListRequest(ParsedCommand parsedCommand) {
//        Получает и разбирает аргументы команды res_list
        String arguments = parsedCommand.arguments();
        if (arguments.isEmpty()) {
            throw new ValidationException("res_list requires run id");
        }

        String[] parts = arguments.split("\\s+");
        if (parts.length != 1 && parts.length != 3) {
            throw new ValidationException("res_list accepts: <runId> or <runId> --param PARAM");
        }

        long runId;
        try {
//            Проверяет что runId - число, перевод в long
            runId = Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            throw new ValidationException("run id must be a number");
        }

        if (parts.length == 1) {
            return new ResultListRequest(runId, null);
        }

        if (!"--param".equals(parts[1])) {
            throw new ValidationException("res_list accepts only the --param option");
        }

        MeasurementParam param = parseMeasurementParam(parts[2], "param");
        return new ResultListRequest(runId, param);
    }

    private MeasurementParam parseMeasurementParam(String rawValue, String label) {
        for (MeasurementParam param : MeasurementParam.values()) {
            if (param.name().equalsIgnoreCase(rawValue)) {
                return param;
            }
        }

        throw new ValidationException(label + " must be one of: pH, Temperature, Concentration.");
    }

    //    Форматированный вывод списка результатов
    private String formatResultLine(RunResult result) {
        return result.getId()
                + " | "
                + " | param - " + result.getParam()
                + " | value - " + result.getValue()
                + " | unit - " + result.getUnit()
                + " | comment - " + formatNullableValue(result.getComment());
    }

    //    Команда res_list - показать список результатов
    private void handleResultList(ParsedCommand parsedCommand) {
//        Через парсер берёт runId и param, находит run
        ResultListRequest request = parseResultListRequest(parsedCommand);
        Run run = runService.getById(request.runId());
//        Записываем в коллекцию результаты конкретного прогона
        var results = runResultService.listByRunId(request.runId());

//        Если передан param, то фильтрует список по нему
        if (request.param() != null) {
            results = results.stream()
                    .filter(result -> result.getParam() == request.param())
                    .toList();
        }

        if (results.isEmpty()) {
            out.println("No results found for run " + run.getId() + ".");
            return;
        }

        out.println("Results for run " + run.getId() + " (" + run.getName() + "):");
        for (RunResult result : results) {
            out.println(formatResultLine(result));
        }
    }

    private String formatSummaryLine(MeasurementParam param, List<Double> values) {
        int count = values.size();
        double min = values.get(0);
        double max = values.get(0);
        double sum = 0.0;

        for (double value : values) {
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
            sum += value;
        }

        double avg = sum / count;
        return param
                + ": count=" + count
                + " min=" + formatDecimal(min)
                + " max=" + formatDecimal(max)
                + " avg=" + formatDecimal(avg);
    }

    private String formatDecimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private void handleExperimentSummary(ParsedCommand parsedCommand) {

//        Через парсер находим id эксперимента
        long experimentId = parseRequiredLongArgument(parsedCommand, "exp_summary", "experiment id");
        Experiment experiment = experimentService.getById(experimentId);
//        Вызываем список прогонов этого эксперимента, чтобы сделать summary
        var runs = runService.listByExpId(experimentId);

//        Собираем все числовые значение результатов по типу параметра
        Map<MeasurementParam, List<Double>> valuesByParam = new EnumMap<>(MeasurementParam.class);
        for (Run run : runs) {
//            Для каждого прогона берутся его результаты и группируются по параметру
            for (RunResult result : runResultService.listByRunId(run.getId())) {
                valuesByParam
//                        Создаём список для параметра, если его еще нет
                        .computeIfAbsent(result.getParam(), key -> new ArrayList<>())
                        .add(result.getValue());
            }
        }

        if (valuesByParam.isEmpty()) {
//            Если в прогонах нет результатов
            out.println("No summary data for experiment " + experiment.getId() + ".");
            return;
        }

//        Порядок вывода как в enum, форматированный вывод
        out.println("Summary for experiment " + experiment.getId() + " (" + experiment.getName() + "):");
        for (MeasurementParam param : MeasurementParam.values()) {
            List<Double> values = valuesByParam.get(param);
            if (values != null && !values.isEmpty()) {
                out.println(formatSummaryLine(param, values));
            }
        }
    }

    private record ExperimentUpdateRequest(long id, String field, String value) {
    }

    private record ResultListRequest(long runId, MeasurementParam param) {
    }

    private record ParsedCommand(String name, String arguments) {
    }

    // 3 ЭТАП: JSON
    //Метод сохранение данных в JSON, если что то не так выбрасываем ошибку
    private void handleSave(ParsedCommand command) {
        String path = extractSingleArgument(command, "save");

        try {
            dataManager.saveToFile(path);
            currentFilePath = path;
            out.println("Data saved to " + path);
        } catch (IOException e) {
            throw new ValidationException("Failed to save file: " + e.getMessage());
        }
    }

    // ===== 3 ЭТАП: JSON =====
    //Метод для сохраннения данных из JSON, если что то не так выбрасываем ошибку
    private void handleLoad(ParsedCommand command) {
        String path = extractSingleArgument(command, "load");

        try {
            dataManager.loadFromFile(path);
            currentFilePath = path;
            out.println("Data loaded from " + path);
        } catch (IOException e) {
            throw new ValidationException("Failed to read file: " + e.getMessage());
        } catch (ValidationException e) {
            throw new ValidationException("Invalid file content: " + e.getMessage());
        }
    }

    // ===== 3 ЭТАП: JSON =====
    //Метод проверяющий что путь передан и возвращет этот же путь, если пути нет ошибка
    private String extractSingleArgument(ParsedCommand command, String commandName) {
        if (command.arguments.isEmpty()) {
            throw new ValidationException(commandName + " requires a file path");
        }

        return command.arguments;
    }
}

