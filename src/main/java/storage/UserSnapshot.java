package storage;

import java.util.List;

//Создаем хранилище всех списков для дальнейшего сохранения и загрузки
public class UserSnapshot {
    private List<UserData> users;

    public  UserSnapshot() {
    }

    public UserSnapshot(List<UserData> users) {
        this.users = users;
    }
    public List<UserData> getUsers() {
        return users;
    }
    public void setUsers(List<UserData> users) {
        this.users = users;
    }
}
