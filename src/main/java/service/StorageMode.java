package service;

import java.io.IOException;

// интерфейс описывает общий сценарий для разных режимов хранения

public interface StorageMode {
    String getName(); // =вернуть название режима (DB/Json)
    boolean isDatabase(); // режим=бд?
    void refresh(); // обновить данные из текущего источника (DB/память)
    String save(String path) throws IOException;
    String load(String path) throws IOException;
}
