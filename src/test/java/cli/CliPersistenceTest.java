package cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CliPersistenceTest {

    @TempDir
    //Создаем временную папку
    Path tempDir;

    @Test
        //Проверка, что пользователь может создать и сохранить данные JSON и потом загрузить
    void shouldSaveAndLoadWithCli() throws Exception {
        Path file = tempDir.resolve("data.json");
        Path usersFile = tempDir.resolve("users.json");
        String login = "cli_user_" + System.nanoTime();

        String input = String.join(System.lineSeparator(),
                "register",
                login,
                "password",
                "login",
                login,
                "password",
                "exp_add",
                "Exp",
                "desc",
                "run_add 1",
                "Run",
                "operator",
                "res_add 1",
                "pH",
                "7.0",
                "pH",
                "ok",
                "save_as " + file,
                "load " + file,
                "exp_list",
                "run_list 1",
                "res_list 1",
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out, null, usersFile.toString()).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);

        assertTrue(Files.exists(file));
        assertTrue(output.contains("Data saved to"));
        assertTrue(output.contains("Data loaded from"));
        assertTrue(output.contains("Exp"));
        assertTrue(output.contains("Run"));
        assertTrue(output.contains("pH"));
        assertTrue(output.contains("7.0"));
    }

    @Test
        //Проверяем, что битый JSON ловится
    void shouldShowValidationErrorForInvalidJson() throws Exception {
        Path file = tempDir.resolve("bad.json");
        Files.writeString(file, """
                {
                  "experiments": [],
                  "runs": [
                    {
                      "id": 1,
                      "experimentId": 999,
                      "name": "Broken run",
                      "operatorName": "operator",
                      "createdAt": "2026-04-21T10:00:00Z",
                      "updatedAt": "2026-04-21T10:00:00Z"
                    }
                  ],
                  "runResults": []
                }
                """);

        String input = String.join(System.lineSeparator(),
                "load " + file,
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out, null, tempDir.resolve("users.json").toString()).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);

        assertTrue(output.contains("Validation error"));
        assertTrue(output.contains("Invalid file content"));
    }
}
