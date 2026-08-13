package ru.itone.illya4gurenko.service;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("test multipart transfer HTTP Multipart ")
class ApacheMultipartSenderServiceTest {

    private HttpServer mockHttpServer;
    private final int testPort = 8092;
    private Path tempFile;
    private final AtomicBoolean isMultipartHeaderFound = new AtomicBoolean(false);
    private final AtomicBoolean isFileContentReceived = new AtomicBoolean(false);

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("multipart-test-", ".txt");
        Files.writeString(tempFile, "H 20260101 IMMEDIATE\nMULTIPART TEST CONTENT\nT 1");

        mockHttpServer = HttpServer.create(new InetSocketAddress(testPort), 0);
        mockHttpServer.createContext("/upload/multipart", exchange -> {
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");

            if (contentType != null && contentType.contains("multipart/form-data")) {
                isMultipartHeaderFound.set(true);
            }

            byte[] body = exchange.getRequestBody().readAllBytes();
            if (body.length > 0) {
                isFileContentReceived.set(true);
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
    @DisplayName("success test multipart transfer")
    void testSendFileMultipartSuccess() {
        String targetUrl = "http://localhost:" + testPort + "/upload/multipart";

        assertDoesNotThrow(() -> {
            ApacheMultipartSenderService.getInstance().sendFile(tempFile, targetUrl);
        });

        assertTrue(isMultipartHeaderFound.get(), "Заголовок Content-Type должен содержать multipart/form-data");
        assertTrue(isFileContentReceived.get(), "Тело запроса с файлом должно быть передано серверу");
    }
}