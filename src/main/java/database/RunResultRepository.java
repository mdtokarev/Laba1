package database;

import domain.MeasurementParam;
import domain.RunResult;
import validation.ValidationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

// класс для работы с таблицей run_results из БД
public class RunResultRepository {
    private final Database database;

    public RunResultRepository(Database database) {
        this.database = database;
    }

    public List<RunResult> findAll() {
//        читаем из таблицы run_results только нужные колонки и сортируем по id
        String sql = "select id, run_id, param, value, unit, comment, created_at, updated_at from run_results order by id";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
//            пока строки есть, читаем их и превращаем в объекты RunResult
            List<RunResult> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(toRunResult(resultSet));
            }
            return results;

        } catch (SQLException e) {
            throw new ValidationException("Failed to load results: " + e.getMessage());
        }
    }

//    маппер строки из БД в доменный объект RunResult
    private RunResult toRunResult(ResultSet resultSet) throws SQLException {

//        используем .restore, чтобы брать уже существующий объект из DB/Json и собирать его в объект RunResult
        return RunResult.restore(
                resultSet.getLong("id"),
                resultSet.getLong("run_id"),
                MeasurementParam.valueOf(resultSet.getString("param")),
                resultSet.getDouble("value"),
                resultSet.getString("unit"),
                resultSet.getString("comment"),
                resultSet.getTimestamp("created_at").toInstant(),
                resultSet.getTimestamp("updated_at").toInstant()
        );
    }

    public RunResult insert(long runId, MeasurementParam param, double value, String unit, String comment) {

        String sql = """
                insert into run_results(run_id, param, value, unit, comment, created_at, updated_at)
                values (?, ?, ?, ?, ?, current_timestamp, current_timestamp)
                returning id, run_id, param, value, unit, comment, created_at, updated_at
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, runId);
            statement.setString(2, param.name()); // превращает enum в строку
            statement.setDouble(3, value);
            statement.setString(4, unit);
            statement.setString(5, comment);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return toRunResult(resultSet);
            }

        } catch (SQLException e) {
            throw new ValidationException("Failed to create result: " + e.getMessage());
        }
    }

    public void update(RunResult result) {

        String sql = "update run_results set param = ?, value = ?, unit = ?, comment = ?, updated_at = ? where id = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, result.getParam().name());
            statement.setDouble(2, result.getValue());
            statement.setString(3, result.getUnit());
            statement.setString(4, result.getComment());
            statement.setTimestamp(5, Timestamp.from(result.getUpdatedAt()));
            statement.setLong(6, result.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new ValidationException("Failed to update result: " + e.getMessage());
        }
    }

    public void delete(long id) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("delete from run_results where id = ?")) {

            statement.setLong(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new ValidationException("Failed to delete result: " + e.getMessage());
        }
    }
}
