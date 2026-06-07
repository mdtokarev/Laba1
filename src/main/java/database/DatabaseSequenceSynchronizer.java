package database;

import validation.ValidationException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSequenceSynchronizer {
    private final Database database; // через этот объект получаем соединение с БД

    public DatabaseSequenceSynchronizer(Database database) {
        this.database = database;
    }

    public void syncAll() {
        // метод синхронизации всех счетчиков - вызывается при старте и refresh в ui
        syncUsers();
        syncExperiments();
        syncRuns();
        syncRunResults();
    }

    public void syncUsers() {
        // ф-я setval вручную выставляет значение счетчика
        // далее находим sequence, который привязан к колонке users.id, выбираем максимальный id
        // если таблица пустая - nextval() вернет id=1, если не пустая - вернет max(id)+1
        executeSequenceSync("""
                select setval(
                    pg_get_serial_sequence('users', 'id'),
                    coalesce((select max(id) from users), 1),
                    (select max(id) from users) is not null
                )
                """, "users");
    }

    public void syncExperiments() {
        executeSequenceSync("""
                select setval(
                    pg_get_serial_sequence('experiments', 'id'),
                    coalesce((select max(id) from experiments), 1),
                    (select max(id) from experiments) is not null
                )
                """, "experiments");
    }

    public void syncRuns() {
        executeSequenceSync("""
                select setval(
                    pg_get_serial_sequence('runs', 'id'),
                    coalesce((select max(id) from runs), 1),
                    (select max(id) from runs) is not null
                )
                """, "runs");
    }

    public void syncRunResults() {
        executeSequenceSync("""
                select setval(
                    pg_get_serial_sequence('run_results', 'id'),
                    coalesce((select max(id) from run_results), 1),
                    (select max(id) from run_results) is not null
                )
                """, "run_results");
    }

    // метод выполнения sql-запроса
    private void executeSequenceSync(String sql, String tableName) {
        try (Connection connection = database.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new ValidationException("Failed to sync sequence for " + tableName + ": " + e.getMessage());
        }
    }
}
