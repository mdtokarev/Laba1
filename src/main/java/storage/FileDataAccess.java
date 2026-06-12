package storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import domain.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

//Хранилище данных для сохранения в файл
public class FileDataAccess {
    //После mapper объект  Jackson, с которым мы и работаем при сохранении в файл
    private final ObjectMapper mapper;

    public FileDataAccess() {
        //Создаем ObjectMapper и включаем красивый формат JSON
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    //Метод для сохранения основных лабораторных данных
    public void saveData(Path path, DataSnapshot snapshot) throws IOException {
        //Перед сохранением создаем папки, если их нет
        createParentDirectories(path);
        //Записываем данные в файл
        mapper.writeValue(path.toFile(), snapshot);
    }

    //Метод загружает основные данные из файла
    public DataSnapshot loadData(Path path) throws IOException {
        //Читаем файл и превращаем объекты в DataSnapshot
        return mapper.readValue(path.toFile(), DataSnapshot.class);
    }

    //Метод сохранения пользователей
    public void saveUsers(Path path, List<User> users) throws IOException {
        //Перед сохранением создаем папки, если их нет
        createParentDirectories(path);

        //Проходим по каждому пользователю и превращаем User в список UserData
        UserSnapshot snapshot = new UserSnapshot(users.stream().map(this::toUserData).toList());
        //Записываем пользователей в JSON-файл
        mapper.writeValue(path.toFile(), snapshot);
    }

    //Метод загружает пользователей из файла
    public List<User> loadUsers(Path path) throws IOException {
        //Если файла пользователей еще нет, возвращаем пустой список
        if (!Files.exists(path)) {
            return List.of();
        }

        //Проходим по каждому пользователю и превращаем в UserSnapshot
        UserSnapshot snapshot = mapper.readValue(path.toFile(), UserSnapshot.class);

       //Если в файле нет секции users возвращаем пустой список
        if (snapshot.getUsers() == null) {
            return List.of();
        }

        //Берем List<UserData> из snapshot и переводим обратно в List<User>
        return snapshot.getUsers().stream().map(this::toUser).toList();
    }

    //Метод переводит доменного пользователя в JSON-объект
    private UserData toUserData(User user) {
        return new UserData(user.getId(), user.getLogin(), user.getPasswordHash());
    }

    //Метод переводит из JSON-объект в доменного пользователя
    private User toUser(UserData data) {
        return new User(data.getId(), data.getLogin(), data.getPasswordHash());
    }

    //Метод создает родительские папки для файла
    private void createParentDirectories(Path path) throws IOException {
        //Берем папку в которой должен лежать файл
        Path parent = path.getParent();

        //Проверяем есть ли родительская папка
        if (parent != null) {
            //Создаем папку, если ее нет
            Files.createDirectories(parent);
        }
    }
}
