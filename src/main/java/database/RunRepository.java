package database;

import domain.Run;
import validation.ValidationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

// класс работает с таблицей runs из БД
public class RunRepository {
    private final Database database;

    public RunRepository(Database database) {
        this.database = database;
    }

    public List<Run> findAll() {

        String sql = "select id, experiment_id, name, operator_name, created_at, updated_at from runs order by id";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

//            пока строки есть, читаем их, превращаем в Run и добавляем в список
            List<Run> runs = new ArrayList<>();
            while (resultSet.next()) {
                runs.add(toRun(resultSet));
            }
            return runs;

        } catch (SQLException e) {
            throw new ValidationException("Failed to load runs: " + e.getMessage());
        }
    }

//    маппер строки БД в объект Run: SQL-строка -> java-объект
    private Run toRun(ResultSet resultSet) throws SQLException {
//        используем .restore, чтобы брать уже существующий объект из DB/Json и собирать его в объект Run
        return Run.restore(
                resultSet.getLong("id"),
                resultSet.getLong("experiment_id"),
                resultSet.getString("name"),
                resultSet.getString("operator_name"),
                resultSet.getTimestamp("created_at").toInstant(),
                resultSet.getTimestamp("updated_at").toInstant()
        );
    }

    public Run insert(long experimentId, String name, String operatorName) {

        String sql = """
                insert into runs(experiment_id, name, operator_name, created_at, updated_at)
                values (?, ?, ?, current_timestamp, current_timestamp)
                returning id, experiment_id, name, operator_name, created_at, updated_at
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, experimentId); // передаем в запрос к БД id эксперимента
            statement.setString(2, name);
            statement.setString(3, operatorName);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return toRun(resultSet);
            }

        } catch (SQLException e) {
            throw new ValidationException("Failed to create run: " + e.getMessage());
        }
    }

    public void update(Run run) {

//        в запросе указываем какие колонки хотим обновить и у какого именно id
        String sql = "update runs set name = ?, operator_name = ?, updated_at = ? where id = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, run.getName());
            statement.setString(2, run.getOperatorName());
            statement.setTimestamp(3, Timestamp.from(run.getUpdatedAt()));
            statement.setLong(4, run.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new ValidationException("Failed to update run: " + e.getMessage());
        }
    }

    public void delete(long id) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("delete from runs where id = ?")) {

            statement.setLong(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new ValidationException("Failed to delete run: " + e.getMessage());
        }
    }
}