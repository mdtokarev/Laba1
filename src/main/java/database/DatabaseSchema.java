package database;

import validation.ValidationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSchema {

//    берем готовый механизм подключения
    private final Database database;

    public DatabaseSchema(Database database) {
        this.database = database;
    }

//    создать если нужно - если таблицы уже есть, то повторный запуск не должен все рушить и ломать БД
    public void createIfNeeded() {
        String sql;
        try {
//            ищем файл schema.sql в корне проекта, читаем в строку
            sql = Files.readString(Path.of("schema.sql"));
        } catch (IOException e) {
            throw new ValidationException("Failed to read schema.sql: " + e.getMessage());
        }

//        открываем JDBC-соединение, на его основе создаем statement, sql из файла выполняется целиком
        try (Connection connection = database.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);

            /* здесь statement - объект JDBC для выполнения sql. используется, тк schema.sql -
            * файл с фиксированной sql-структурой.
            * иными словами - это интерфейс для отправки статических sql-запросов к БД */

        } catch (SQLException e) {
            throw new ValidationException("Failed to create database schema: " + e.getMessage());
        }
    }
}
