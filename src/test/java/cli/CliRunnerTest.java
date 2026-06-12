package cli;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CliRunnerTest {

    @Test
    void shouldAddExperimentViaInteractiveCommand() {
        String login = "cli_user_" + System.nanoTime();
        String usersFile = "build/test-users-" + login + ".json";

        String input = String.join(System.lineSeparator(),
                "register",
                login,
                "password",
                "login",
                login,
                "password",
                "exp_add",
                "Experiment A",
                "Description A",
                "exit"
        ) + System.lineSeparator();

//        Даём раннеру строки так, будто бы их вводит пользователь
        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);

        new CliRunner(inputStream, out, null, usersFile).start();

        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("User registered with id "));
        assertTrue(output.contains("Logged in as " + login));
        assertTrue(output.contains("Creating a new experiment."));
        assertTrue(output.contains("Experiment created with id "));
        assertTrue(output.contains("CLI stopped."));
    }
}
