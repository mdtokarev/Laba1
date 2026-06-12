package domain;

import validation.ValidationException;

public class User {
    private final long id;
    private final String login;
    private final String passwordHash;

    public User(long id, String login, String passwordHash) {
        //Валидируем наши данные
        validateId(id);
        validateLogin(login);
        validatePasswordHash(passwordHash);

        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
    }

    //Проверка что ID > 0, если нет ошибка
    private static void validateId(long id){
        if (id <= 0) {
            throw new ValidationException("User id must be positive");
        }
    }

    //Проверка что логин не null и не пустой и не больше 64, либо ошибка
    private static void validateLogin(String login){
        if (login == null || login.isBlank()) {
            throw new ValidationException("Login can't be empty");
        }
        if (login.length() > 64) {
            throw new ValidationException("Login too long");
        }
    }

    //Проверка что хеш не должен быть null, пустым или только из пробелов
    private static void validatePasswordHash(String passwordHash){
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new ValidationException("Password hash can't be empty");
        }
    }

    public long getId() {
        return id;
    }
    public String getLogin() {
        return login;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
}
