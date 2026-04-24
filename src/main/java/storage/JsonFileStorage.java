package storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// Этот класс только читает и пишет JSON-файл
public class JsonFileStorage {
    private final ObjectMapper mapper;

    public JsonFileStorage() {
        this.mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);//Удобное форматирование
    }
//Метод для сохранения
    public void save(Path path, DataSnapshot snapshot) throws IOException {
        Path parent = path.getParent();//Берем родительскую папку файла, сели есть создаем файл. Если нет родительской то создаст ее

        if (parent != null) {
            Files.createDirectories(parent);
        }

        mapper.writeValue(path.toFile(), snapshot);//Записиваем JSON
    }

    public DataSnapshot load(Path path) throws IOException {
        return mapper.readValue(path.toFile(), DataSnapshot.class);//Возвращаем из JSON в DataSnapshot
    }
}
