package database;

import domain.Experiment;
import validation.ValidationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// класс работает с таблицей experiments из БД
public class ExperimentRepository {
    private  final Database database;

    public ExperimentRepository(Database database) {
        this.database = database;
    }

    public List<Experiment> findAll() {

        String sql = "select id, name, description, owner_id, created_at, updated_at from experiments order by id";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            List<Experiment> experiments = new ArrayList<>();
            while (resultSet.next()) {
                experiments.add(toExperiment(resultSet));
            }
            return experiments;

        } catch (SQLException e) {
            throw new ValidationException("Failed to load experiments: " + e.getMessage());
        }
    }

//    маппер SQl-строки в java-объект Experiment
    private Experiment toExperiment(ResultSet resultSet) throws SQLException {
//        используем .restore, чтобы брать уже существующий объект из DB/Json и собирать его в объект Experiment
        return Experiment.restore(
//                из текущей строки берем следующие данные:
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getLong("owner_id"),
//                преобразуем Timestamp (DB) -> Instant (domain):
                resultSet.getTimestamp("created_at").toInstant(),
                resultSet.getTimestamp("updated_at").toInstant()
        );
    }

    public Experiment insert(String name, String description, long ownerId) {

//        owner_id приходит как параметр - id текущего авторизованного пользователя
//        БД сама ставит время created_at & updated_at
        String sql = """
                insert into experiments(name, description, owner_id, created_at, updated_at)
                values (?, ?, ?, current_timestamp, current_timestamp)
                returning id, name, description, owner_id, created_at, updated_at
                """;

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, description);
            statement.setLong(3, ownerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return toExperiment(resultSet);
            }

        } catch (SQLException e) {
            throw new ValidationException("Failed to create experiment: " + e.getMessage());
        }
    }

//    метод отправляет изменения в БД
    public void update(Experiment experiment) {

//        в запросе указываем, какие колонки хотим обновить, и у какого именно id
        String sql = "update experiments set name = ?, description = ?, updated_at = ? where id = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, experiment.getName());
            statement.setString(2, experiment.getDescription());
            statement.setTimestamp(3, Timestamp.from(experiment.getUpdatedAt()));
            statement.setLong(4, experiment.getId());
            statement.executeUpdate(); // отправляем обновление в БД

        } catch (SQLException e) {
            throw new ValidationException("Failed to update experiment: " + e.getMessage());
        }
    }

    public void delete(long id) {

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("delete from experiments where id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new ValidationException("Failed to delete experiment: " + e.getMessage());
        }
    }

}
