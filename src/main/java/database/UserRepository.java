package database;

import domain.User;
import validation.ValidationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// класс, который будет работать с таблицей users, он загружает пользователей из БД
// зона ответственности - sql для users
public class UserRepository {
    private final Database database;
    private final DatabaseSequenceSynchronizer sequenceSynchronizer;

    public UserRepository(Database database) {
        this.database = database;
        this.sequenceSynchronizer = new DatabaseSequenceSynchronizer(database);
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

        sequenceSynchronizer.syncUsers();
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

//    восстановление пользователя с уже готовым id - существующий объект user положить в БД с тем же id
    public void insertRestored(User user) {

//        если в таблице уже есть строка с таким id - не падать с ошибкой, а просто ничего не делать
        String sql = "insert into users(id, login, password_hash) values (?, ?, ?) on conflict (id) do nothing";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, user.getId()); // сами указываем id в sql-запрос
            statement.setString(2, user.getLogin());
            statement.setString(3, user.getPasswordHash());

            statement.executeUpdate(); // просто пытаемся вставить строку в таблицу - нам не нужно ResultSet
            syncSequence(connection);

        } catch (SQLException e) {
            throw new ValidationException("Failed to restore user: " + e.getMessage());
        }
    }

    /* выдача айди в БД работает через sequence внутри postgreSQL,
    * если произойдет ситауция, что в таблице есть id=1,2,3 а мы хотим вставить пользователя с id=10,
    * sequence не будет знать что с этим делать - для него следующий id=4
    * новый метод синхронизирует sequence и обновляет внутренний счетчик postgreSQL */

    private  void syncSequence(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {

//            если таблица не пустая - берем максимальный id, если пустая - берем 1
//            выставляем счетчик id на текущее максимальное значение в таблице
            statement.execute("select setval(pg_set_serial_sequence('users', 'id'), coalesce((select max(id) from users), 1), true)");
        }
    }
}
