package ru.itone.illya4gurenko.service;

import java.io.IOException;
import java.nio.file.Path;

public interface FileSender {
    void sendFile(Path filePath, String targetUrl) throws IOException;
}
