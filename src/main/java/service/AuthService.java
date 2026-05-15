package service;

import domain.User;
import validation.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//Класс для авторизации
public class AuthService {
    //Хранилище пользователей
    private final Map<Long, User> users = new TreeMap<>();
    //Не храним обычный пароль, хешируем пароль
    private final PasswordHasher passwordHasher = new PasswordHasher();

    //Первый пользователь id 1
    private long nextId = 1;
    private User currentUser;

    //Метод регистрации
    public User register(String login, String password) {
        //Проверяем что пользователя с таким логином нет или ошибка
        if (findByLogin(login) != null){
            throw new ValidationException("Login is already taken");
        }

        //Берем следующий id и увеличиваем счетчик
        long id = nextId++;
        //Хешируем пароль
        String passwordHash = passwordHasher.hash(password);

        //Создаем пользователя
        User user = new User(id, login, passwordHash);
        //Кладем его в коллекцию
        users.put(id, user);

        //Возвращаем созданного
        return user;
    }

    //Метод входа
    public User login(String login, String password) {
        //Ищем пользователя по логину
        User user = findByLogin(login);

        //Если пользователь не найден ошибка
        if (user == null) {
            throw new ValidationException("Invalid login or password");
        }

        //Хешируем введеный пароль
        String passwordHash = passwordHasher.hash(password);

        //Сравниваем хеши если не совпадают ошибка
        if (!user.getPasswordHash().equals(passwordHash)) {
            throw new ValidationException("Invalid login or password");
        }

        //Пользователь становится текущим
        currentUser = user;
        //Возвращаем пользователя
        return user;
    }

    //Метод выхода
    public void logout() {
        //Обнуляем текущего пользователя
        currentUser = null;
    }

    //Создаем копию мписка пользователей
    public List<User> list(){
        return new ArrayList<>(users.values());
    }

    //Копия текущих пользователей для сохранения в файл
    public List<User> snapshot(){
        return new ArrayList<>(users.values());
    }

    //Метод загрузки пользователей из файла
    public void loadRestored(List<User> restoredUsers ){
        //Создаем временную коллекцию для проверки данных
        Map<Long, User> loadedUsers = new TreeMap<>();
        long maxId = 0;

        //Проходим по всем пользователям
        for (User user : restoredUsers){
            //Проверяем что логин не повторяется или ошибка
            if (findByLoginInMap(loadedUsers, user.getLogin()) != null){
                throw new ValidationException("Duplicate user login: " + user.getLogin());
            }
            //Складываем пользавателей по id  если повторяются ошибка
            if (loadedUsers.put(user.getId(), user) != null){
                throw new ValidationException("Duplicate user id: " + user.getId());
            }

            //Запоминаем самый большой id
            maxId = Math.max(maxId, user.getId());
        }

        //Очищаем текущую коллекцию
        users.clear();
        //Загружаем провернные данные
        users.putAll( loadedUsers );
        //Устанавлеваем следующий id
        nextId = maxId + 1;
        //Обнуляем текущего пользователя
        currentUser = null;
    }

    //Метод возращаем текущего пользователя
    public User getCurrentUser() {
        return currentUser;
    }

    //Метод требует авторизации пользователя
    public User requireCurrentUser() {
        //Если нет текущего пользователя ошибка
        if (currentUser == null) {
            throw new ValidationException("You need to login first");
        }

        //Возращаем текущего
        return currentUser;
    }

    //Метод ищет пользователя по логину в коллекции
    private User findByLogin(String login){
        return findByLoginInMap(users, login);
    }

    //Поиск пользователя по логину в коллекции
    private User findByLoginInMap(Map<Long, User> source, String login){
        //Проходим по всем пользователям в коллекции
        for (User user : source.values()){
            //Проверяем сходство логина
            if (user.getLogin().equals(login)){
                //Возращаем
                return user;
            }
        }
        return null;
    }
}
