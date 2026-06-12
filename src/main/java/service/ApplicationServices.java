package service;

import database.*;

public class ApplicationServices {
//    флаг режима запуска: true -> работаем через postgreSQL, false -> работаем по старой схеме локально
    private final boolean databaseEnabled;

//    объявляем все основные сервисы приложения
    private final ExperimentService experimentService;
    private final RunService runService;
    private final RunResultService runResultService;
    private final AuthService authService;
    private final AccessControlService accessControlService;
    private final LabService labService;
    private final DataManager dataManager;
    private final ExperimentSummaryService summaryService;
    private final DatabaseSequenceSynchronizer sequenceSynchronizer;
    private final StorageMode storageMode;

    public ApplicationServices() {
//        читаем db.properties -> создаем объект Database; узнаем статус БД (on/off)
        DatabaseContext context = DatabaseContext.loadDefault();
        this.databaseEnabled = context.isEnabled(); // сохраняем статус БД

        if (databaseEnabled) {
            Database database = context.getDatabase(); // берем объект, который может создавать JDBC-подключение
            this.sequenceSynchronizer = new DatabaseSequenceSynchronizer(database);
//            создаем репозиторий для таблицы users, передаем в сервис авторизации - далее работа через postgreSQL
            this.authService = new AuthService(new UserRepository(database));

            this.experimentService = new ExperimentService(new ExperimentRepository(database));
//            RunService зависит от ExperimentService -> проверяем существование родителя
            this.runService = new RunService(experimentService, new RunRepository(database));

            this.runResultService = new RunResultService(runService, new RunResultRepository(database));
        } else {
            this.sequenceSynchronizer = null;
//            если БД не включена, то создаем сервисы по старой схеме (с сохранением зависимостей)
            this.experimentService = new ExperimentService();
            this.runService = new RunService(experimentService);
            this.runResultService = new RunResultService(runService);
            this.authService = new AuthService();
        }

//        создание общих сервисов, работающих поверх собранного приложения, им нужны все доменные сервисы
        this.accessControlService = new AccessControlService(experimentService, runService, runResultService);
        this.labService = new LabService(experimentService, runService, runResultService);
        this.dataManager = new DataManager(experimentService, runService, runResultService, authService);
        this.summaryService = new ExperimentSummaryService(experimentService, runService, runResultService);

        if (databaseEnabled) {
            this.storageMode = new DatabaseStorageMode(sequenceSynchronizer, authService, experimentService, runService, runResultService);
        } else {
            this.storageMode = new FileStorageMode(dataManager);
        }
    }

    public boolean isDatabaseEnabled() {
        return databaseEnabled;
    }
    public ExperimentService getExperimentService() {
        return experimentService;
    }
    public RunService getRunService() {
        return runService;
    }
    public RunResultService getRunResultService() {
        return runResultService;
    }
    public AuthService getAuthService() {
        return authService;
    }
    public AccessControlService getAccessControlService() {
        return accessControlService;
    }
    public LabService getLabService() {
        return labService;
    }
    public DataManager getDataManager() {
        return dataManager;
    }
    public ExperimentSummaryService getSummaryService() {
        return summaryService;
    }
    public DatabaseSequenceSynchronizer getSequenceSynchronizer() {
        return sequenceSynchronizer;
    }
    public StorageMode getStorageMode() {
        return storageMode;
    }
}
