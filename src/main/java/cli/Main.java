package cli;

public class Main {
    public static void main(String[] args) {
        // 3 ЭТАП: JSON
        // Если путь к JSON передали аргументом при запуске, CLI попробует загрузить данные сразу
        String initialFilePath = null;

        if (args.length > 0) {
            initialFilePath = args[0];
        }

        new CliRunner(System.in, System.out, initialFilePath).start();
    }
}