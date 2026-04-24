package domain;

import validation.ValidationException;

import java.time.Instant;

public final class Experiment {
    // Уникальный номер эксперимента. Программа назначает сама.
    private final long id;
    // Название эксперимента. Нельзя пустое. До 128 символов.
    private String name;
    // Описание (кратко “что делаем”). Можно пусто. До 512 символов.
    private String description;
    // Кто создал (логин). На ранних этапах можно "SYSTEM".
    private String ownerUsername;
    // Когда создан. Программа ставит автоматически.
    private final Instant createdAt;
    // Когда изменяли. Программа обновляет автоматически.
    private Instant updatedAt;

    public Experiment(long id, String name, String description, String ownerUsername) {
        this(id, name, description, ownerUsername, Instant.now(), Instant.now());
    }


    private Experiment(long id, String name, String description, String ownerUsername, Instant createdAt, Instant updatedAt) {
        validateId(id);
        validateName(name);
        validateDescription(description);
        validateOwnerUsername(ownerUsername);
        validateTimestamps(createdAt, updatedAt);

        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerUsername = ownerUsername;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    private static void validateId(long id) {
        if (id <= 0)
            throw new ValidationException("Experiment ID must be positive");
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank())
            throw new ValidationException("Experiment name can't be empty");
        if (name.length() > 128)
            throw new ValidationException("Experiment name too long");
    }

    private static void validateDescription(String description) {
        if (description != null && description.length() > 512)
            throw new ValidationException("Description too long");
    }

    private static void validateOwnerUsername(String ownerUsername) {
        if (ownerUsername == null || ownerUsername.isBlank())
            throw new ValidationException("OwnerUsername can't be empty");
        if (ownerUsername.length() > 128)
            throw new ValidationException("OwnerUsername too long");
    }



    public void setName(String name) {
        validateName(name);
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public void setDescription(String description) {
        validateDescription(description);
        this.description = description;
        this.updatedAt = Instant.now();
    }

    public void setOwnerUsername(String ownerUsername) {
        validateOwnerUsername(ownerUsername);
        this.ownerUsername = ownerUsername;
        this.updatedAt = Instant.now();
    }

/*    Выносим метод обновления из сервиса в доменный класс, тк он должен
      безопасно и корректно менять своё состояние, и не имеет отношения к коллекции */
    public void update(String name, String description, String ownerUsername) {
        validateName(name);
        validateDescription(description);
        validateOwnerUsername(ownerUsername);

        this.name = name;
        this.description = description;
        this.ownerUsername = ownerUsername;
        this.updatedAt = Instant.now();
    }

    public long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public String getOwnerUsername() {
        return ownerUsername;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    //3 ЭТАП: JSON
    //Проверка коректонсти времени
    private static void validateTimestamps(Instant createdAt, Instant updatedAt) {
        if (createdAt == null)
            throw new ValidationException("Experiment createdAt can't be null");
        if (updatedAt == null)
            throw new ValidationException("Experiment updatedAt can't be null");
        if (updatedAt.isBefore(createdAt)) {
            throw new ValidationException("Experiment updatedAt can't be before createdAt");
        }
    }

    // 3 ЭТАП: JSON
    //Метод для востановления объекта из JSON
    public static Experiment restore(long id, String name, String description, String ownerUsername, Instant createdAt, Instant updatedAt) {
        return new Experiment(id, name, description, ownerUsername, createdAt, updatedAt);
    }

}


