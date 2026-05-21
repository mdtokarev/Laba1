package database;

import domain.User;
import validation.ValidationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// класс, который будет работать с таблицей users, он загружает пользователей из БД
// зона ответственности - sql для users
public class UserRepository {
    private final Database database;

    public UserRepository(Database database) {
        this.database = database;
    }

    public List<User> findAll() {
//        пишем sql-select-запрос - читаем таблицу users, выбираем нужные колонки и сортируем по id
        String sql = "select id, login, password_hash from users order by id";
        try (Connection connection = database.getConnection();

//             создаем JDBC-объект для выполнения запросов с возможностью подстановки параметров через ?
             PreparedStatement statement = connection.prepareStatement(sql);

//             вводим sql-представление результата (строки БД), выполняем select-запрос
//             ResultSet - курсор по строкам таблицы
             ResultSet resultSet = statement.executeQuery()) {

//            пока строки есть, читаем их, превращаем в User и добавляем в список
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(toUser(resultSet));
            }
            return users; // список всех объектов User

        } catch (SQLException e) {
            throw new ValidationException("Failed to load users: " + e.getMessage());
        }
    }

//    маппер строки БД в объект User: SQL-строка -> java-объект
    private User toUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("login"),
                resultSet.getString("password_hash")
        );
    }

    public User findByLogin(String login) {
        String sql = "select id, login, password_hash from users where login = ?";
//        "?" - заглушка в запросе, означает "значение придет отдельно"
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, login); // в первый "?" подставляем login

            try (ResultSet resultSet = statement.executeQuery()) {

//                если строка нашлась - собираем объект User, если нет - возвращаем null
                if (resultSet.next()) {
                    return toUser(resultSet);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new ValidationException("Failed to find user: " + e.getMessage());
        }
    }

//    создание нового пользователя - login и passwordHash приходят в БД из программы
    public User insert(String login, String passwordHash) {

//        insert-запрос - вставит новую строку в таблицу users
//        sql-запрос с "returning id" -> после insert БД вернет результат как таблицу

        String sql = "insert into users(login, password_hash) values (?, ?) returning id";

        try (Connection connection = database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, login);
            statement.setString(2, passwordHash);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
//                возвращаем новый объект User
                return new User(resultSet.getLong("id"), login, passwordHash);
            }

        } catch (SQLException e) {
            throw new ValidationException("Failed to create user: " + e.getMessage());
        }
    }
}
