package database;

// класс отвечает на вопрос - БД сейчас вообще подключена или нет?
// умеет хранить два состояния - БД вкл/выкл
public class DatabaseContext {
    private final Database database; // объект для JDBC-подключения
    private final boolean enabled; // флаг активности PostgreSQL-режима

    private DatabaseContext(Database database, boolean enabled) {
        this.database = database;
        this.enabled = enabled;
    }

//    создаём корректный контекст по умолчанию
    public static DatabaseContext loadDefault() {
        DatabaseConfig config = DatabaseConfig.loadDefault(); // читаем db.properties
        if (config == null) {
//            создаём контекст в котором нет БД -> не включаем режим PostgreSQL, приложение не падает
            return new DatabaseContext(null, false);
        }

//        создается объект, который может открывать JDBC-соединение
        Database database = new Database(config);
//        автоматическое создание таблиц
        new DatabaseSchema(database).createIfNeeded();
        new DatabaseSequenceSynchronizer(database).syncAll(); // синхро всех счетчиков при старте БД
        return new DatabaseContext(database, true);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Database getDatabase() {
        return database;
    }
}

