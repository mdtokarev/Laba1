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

}
