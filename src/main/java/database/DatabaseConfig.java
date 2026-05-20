package database;

import validation.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/* класс-обёртка вокруг настроек БД, его задача - прочитать db.properties
 проверить значения и отдать их в удобном виде остальному коду */
public class DatabaseConfig {

//    проект по дефолту будет искать файл db.properties, чтобы не дублировать этот путь по всему коду
    private static final Path DEFAULT_PATH = Path.of("db.properties");

    private final String url;
    private final String user;
    private final String password;

    public DatabaseConfig(String url, String user, String password) {

//        прежде чем записать параметры в поля, происходит валидация методом requireValue
        this.url = requireValue(url, "db.url");
        this.user = requireValue(user, "db.user");
        this.password = requireValue(password, "db.password");
    }

//    метод валидации - значение не должно быть пустым
//    здесь value - значение, которое проверяем (url/user/pw), name - имя проверяемого поля (db.url)
    private static String requireValue(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(name + " is required");
        }
        return value;
    }

//    метод загрузки, который читает файл и только потом создаёт объект DatabaseConfig
    public static DatabaseConfig loadDefault() {
        if (!Files.exists(DEFAULT_PATH)) {
            return null; // если нет файла db.properties -> null
        }

//        создём объект для чтения файлов вида db.properties
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(DEFAULT_PATH)) {
            properties.load(input);
        } catch (IOException e) {
            throw new ValidationException("Failed to read db.properties: " + e.getMessage());
        }

//        создаём готовый объект - текстовые значения из properties превращаем в объект DatabaseConfig
        return new DatabaseConfig(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
        );
    }

    public String getUrl() {
        return url;
    }
    public String getUser() {
        return user;
    }
    public String getPassword() {
        return password;
    }
}
