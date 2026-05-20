package database;

import validation.ValidationException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// в этом классе проект идёт в PostgreSQL, его задача - открыть соединение с БД
public class Database {
    private final DatabaseConfig config;

    public Database(DatabaseConfig config) {
        if (config == null) {
            throw new ValidationException("Database config is missing"); // нельзя создать Database без конфига
        }
        this.config = config;
    }

//    метод берет настройки из config, подключается к БД, возвращает готовое соединение
    public Connection getConnection() {
        try {
//            DriverManager ищет JDBC-драйвер, он находится в build.gradle
//            Если у драйвера получилось установить соединение - возвращается объект Connection
            return DriverManager.getConnection(
                    config.getUrl(),
                    config.getUser(),
                    config.getPassword()
            );
        } catch (SQLException e) {
            throw new ValidationException("Failed to contact the database: " + e.getMessage());
        }
    }
}
