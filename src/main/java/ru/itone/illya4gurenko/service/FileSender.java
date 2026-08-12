package ru.itone.illya4gurenko.service;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Стратегия для всех транспортных сервисов отправки файлов.
 * Позволяет взаимозаменяемо использовать gRPC, HTTP Multipart или HTTP Chunked.
 */
public interface FileSender {
    /**
     * Отправляет сгенерированный файл по адресу.
     * @param filePath  Путь к передаваемому файлу на локальном диске
     * @param targetUrl Целевой адрес назначения URL для HTTP или host:port для gRPC
     * @throws IOException Если во время передачи файла возникла сетевая ошибка или ошибка ввода-вывода
     */
    void sendFile(Path filePath, String targetUrl) throws IOException;
}
