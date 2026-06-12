package service;

import validation.ValidationException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//Класс переводит пароль в хеш
public class PasswordHasher {

    //Метод для превода пароля в хеш
    public String hash (String password) {
        //Если пароль пуст то ошибка
        if (password == null || password.isBlank()) {
            throw new ValidationException("Password can't be empty");
        }

        try {
            //Создаем объект для хеширования по алгоритму SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            //Переводим пароль из строки в массив байтов и прогоняем эти байты через SHA-256
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            //Переводим массив байтов в строку из шестнадцатеричных символов
            return toHex(bytes);
            //Ловим ошибку, если алгоритм SHA-256 вдруг недоступен
        } catch (NoSuchAlgorithmException e) {
            throw new ValidationException("Password hashing is not available");
        }
    }

    //Метод переводит массив байтов в строку
    private String toHex(byte[] bytes) {
        //Создаем объект для сборки строки
        StringBuilder result = new StringBuilder();

        //Проходим по каждому байту
        for (byte b : bytes) {
            //Переводим байт в шестнадцатеричный вид и добавляем в строку
            result.append(String.format("%02x", b));
        }

        //Возвращаем готовую строку-хеш
        return result.toString();
    }
}
