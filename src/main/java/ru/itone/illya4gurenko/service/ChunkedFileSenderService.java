package ru.itone.illya4gurenko.service;

import ru.itone.illya4gurenko.config.Base;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class ChunkedFileSenderService extends Base {

    private static final ChunkedFileSenderService INSTANCE = new ChunkedFileSenderService();

    private final HttpClient httpClient;

    private ChunkedFileSenderService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public static ChunkedFileSenderService getInstance() {
        return INSTANCE;
    }

    public void sendFileChunked(Path filePath, String targetUrl) throws IOException {
        info("starting chunked file transfer for: {} to URL: {}", filePath.getFileName(), targetUrl);

        if (!Files.exists(filePath)) {
            error("file not found for chunked transfer: {}", filePath);
            throw new IOException("file not found: " + filePath);
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "text/plain; charset=" + config.getFileCharset().name())
                    .header("X-File-Name", filePath.getFileName().toString())
                    //chunking
                    .POST(HttpRequest.BodyPublishers.ofFile(filePath))
                    .build();

            debug("Sending HTTP request for file: {}", filePath.getFileName());

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                info("File {} successfully transmitted ({} bytes). Server Status: {}",
                        filePath.getFileName(), Files.size(filePath), response.statusCode());
            } else {
                error("Failed to send file {}. Status: {}, Response: {}",
                        filePath.getFileName(), response.statusCode(), response.body());
                throw new IOException("Remote server returned HTTP status: " + response.statusCode());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            error("Chunked file transfer interrupted for file: {}", filePath.getFileName(), e);
            throw new IOException("Transfer interrupted", e);
        } catch (Exception e) {
            error("Error during chunked file transfer for file: {}", filePath.getFileName(), e);
            throw new IOException("Chunked transfer error", e);
        }
    }
}