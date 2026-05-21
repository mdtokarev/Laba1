package ui.dialog;

// вспомогательный класс, переносит данные из окна AuthDialog в UiMain
public class AuthDialogResult {
    private final String action; // что нажал юзер
    private final String login;
    private final String password;

    public AuthDialogResult(String action, String login, String password) {
        this.action = action;
        this.login = login;
        this.password = password;
    }

    public String getAction() {
        return action;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
