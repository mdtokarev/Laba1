package service;

import database.DatabaseSequenceSynchronizer;

// класс db-режима, реализует все методы интерфейса
public class DatabaseStorageMode implements StorageMode {
    private final DatabaseSequenceSynchronizer sequenceSynchronizer; // синхронизатор счетчиков

    // сервисы, нужные бд-режиму
    private final AuthService authService;
    private final ExperimentService experimentService;
    private final RunService runService;
    private final RunResultService runResultService;

    public DatabaseStorageMode(DatabaseSequenceSynchronizer sequenceSynchronizer, AuthService authService,
                               ExperimentService experimentService, RunService runService,
                               RunResultService runResultService) {
        this.sequenceSynchronizer = sequenceSynchronizer;
        this.authService = authService;
        this.experimentService = experimentService;
        this.runService = runService;
        this.runResultService = runResultService;
    }

    @Override
    public String getName() {
        return "PostgreSQL"; // возвращаем имя режима
    }

    @Override
    public boolean isDatabase() {
        return true; // этот режим=бд
    }

    @Override
    public void refresh() {
        sequenceSynchronizer.syncAll(); // синхронизируем счетчики всех таблиц

        // перечитываем данные из бд в сервисы
        authService.refreshFromRepository();
        experimentService.refreshFromRepository();
        runService.refreshFromRepository();
        runResultService.refreshFromRepository();
    }

    // данные сохраняются автоматически - сразу при add/edit/delete
    @Override
    public String save(String path) {
        return "Data is stored automatically in PostgreSQL.";
    }

    // данные читаются автоматически - из бд при старте и refresh
    @Override
    public String load(String path) {
        return "Data is loaded automatically from PostgreSQL.";
    }
}
