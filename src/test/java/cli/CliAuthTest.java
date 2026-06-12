package cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CliAuthTest {

    @TempDir
    Path tempDir;

    @Test
    // Проверяем что без входа нельзя создать эксперимент
    void shouldNotAddExperimentWithoutLogin() {
        String input = String.join(System.lineSeparator(),
                "exp_add",
                "exit"
        ) + System.lineSeparator();

        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out, null, tempDir.resolve("users.json").toString()).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);

        assertTrue(output.contains("Validation error"));
        assertTrue(output.contains("You need to login first"));
    }
}
