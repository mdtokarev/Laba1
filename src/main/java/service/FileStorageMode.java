package service;

import validation.ValidationException;

import java.io.IOException;

// класс file-режима, реализует все методы интерфейса
public class FileStorageMode implements StorageMode {
    private final DataManager dataManager; // объект, умеющий работать с json

    public FileStorageMode(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public String getName() {
        return "JSON file"; // имя режима
    }

    @Override
    public boolean isDatabase() {
        return false; // это не БД-режим
    }

    @Override
    public void refresh() {} // ничего не перечитываем автоматически - все через Load

    @Override
    public String save(String path) throws IOException {
        if (path == null || path.isBlank()) {
            throw new ValidationException("No current file. Use Save As first");
        }

        dataManager.saveToFile(path);
        return "Data saved to:\n" + path;
    }

    @Override
    public String load(String path) throws IOException {
        if (path == null || path.isBlank()) {
            throw new ValidationException("File path can't be empty");
        }

        dataManager.loadFromFile(path);
        return "Data loaded from:\n" + path;
    }
}
