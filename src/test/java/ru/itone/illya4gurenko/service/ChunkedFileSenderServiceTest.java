package ru.itone.illya4gurenko.service;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("test transfer HTTP Chunked")
class ChunkedFileSenderServiceTest {

    private HttpServer mockHttpServer;
    private final int testPort = 8091;
    private Path tempFile;
    private final AtomicBoolean requestReceived = new AtomicBoolean(false);
    private final AtomicReference<String> receivedFileNameHeader = new AtomicReference<>("");

    @BeforeEach
    void setUp() throws IOException {
        // Создаем временный файл
        tempFile = Files.createTempFile("chunked-test-", ".txt");
        Files.writeString(tempFile, "H 20260101 IMMEDIATE\nTEST CLIENT CHUNKED DATA\nT 1");

        // Поднимаем локальный заглушечный HTTP-сервер
        mockHttpServer = HttpServer.create(new InetSocketAddress(testPort), 0);
        mockHttpServer.createContext("/upload/chunked", exchange -> {
            receivedFileNameHeader.set(exchange.getRequestHeaders().getFirst("X-File-Name"));
            byte[] body = exchange.getRequestBody().readAllBytes();

            if (body.length > 0) {
                requestReceived.set(true);
            }

            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        });
        mockHttpServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (mockHttpServer != null) {
            mockHttpServer.stop(0);
        }
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("test transfer HTTP Chunked Raw POST")
    void testSendFileChunkedSuccess() {
        String targetUrl = "http://localhost:" + testPort + "/upload/chunked";

        assertDoesNotThrow(() -> {
            ChunkedFileSenderService.getInstance().sendFile(tempFile, targetUrl);
        });

        assertTrue(requestReceived.get(), "Тестовый HTTP-сервер должен был получить содержимое файла");
        assertEquals(tempFile.getFileName().toString(), receivedFileNameHeader.get(),
                "Заголовок X-File-Name должен содержать имя файла");
    }
}