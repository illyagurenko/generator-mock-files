package ru.itone.illya4gurenko;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import ru.itone.illya4gurenko.dto.ParametersRequestDto;
import ru.itone.illya4gurenko.handler.ParametersHandler;
import ru.itone.illya4gurenko.security.AESCryptoService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@DisplayName("integration test")
class AppIntegrationTest {

    private HttpServer server;
    private Path testKey;
    private Path outputDir;
    private String encryptedPass;
    private final int testPort = 8089;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeAll
    void setUp() throws Exception {
        outputDir = Paths.get("./test_generated");
        Files.createDirectories(outputDir);
        System.setProperty("file.out", outputDir.toString());

        testKey = Files.createTempFile("test-secret", ".key");
        String dummyBase64Key = Base64.getEncoder().encodeToString("1234567890123456".getBytes());
        Files.writeString(testKey, dummyBase64Key);
        System.setProperty("crypto.key", testKey.toAbsolutePath().toString());

        AESCryptoService cryptoService = AESCryptoService.getInstance();
        encryptedPass = cryptoService.encrypt("12345678");

        server = HttpServer.create(new InetSocketAddress(testPort), 0);
        server.createContext("/api/parametres", new ParametersHandler());
        server.setExecutor(null);
        server.start();
    }

    @AfterEach
    void cleanGeneratedFiles() throws IOException {
        if (Files.exists(outputDir)) {
            try (Stream<Path> paths = Files.walk(outputDir)) {
                paths.filter(Files::isRegularFile)
                        .forEach(p -> p.toFile().delete());
            }
        }
    }

    @AfterAll
    void tearDown() throws IOException {
        if (server != null) {
            server.stop(0);
        }
        Files.deleteIfExists(testKey);
        Files.deleteIfExists(outputDir);
    }

    @Test
    @DisplayName("test success request 200 ok")
    void testSuccessFileGeneration() throws Exception {
        ParametersRequestDto testJson = new ParametersRequestDto(
                111,
                222,
                "INTEGRATION",
                10,
                3,
                LocalDateTime.of(2026, 8, 10, 12, 0, 0)
        );

        String jsonStr = mapper.writeValueAsString(testJson);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + testPort + "/api/parametres"))
                .header("Content-Type", "application/json")
                .header("X-Auth-User", "q1")
                .header("X-Auth-Password", encryptedPass)
                .POST(HttpRequest.BodyPublishers.ofString(jsonStr))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "server must request 200 ok");

        Thread.sleep(1500);

        List<Path> generatedFiles;
        try (Stream<Path> paths = Files.list(outputDir)) {
            generatedFiles = paths.filter(Files::isRegularFile).toList();
        }

        System.err.println("======= SERVER RESPONSE DIAGNOSTICS =======");
        System.err.println("STATUS CODE: " + response.statusCode());
        System.err.println("RESPONSE BODY: " + response.body());
        System.err.println("===========================================");
        assertEquals(3, generatedFiles.size(), "must generate 3 files");

        Path firstFile = generatedFiles.get(0);
        List<String> lines = Files.readAllLines(firstFile);

        assertEquals(12, lines.size(), "file must exist 12 record");

    }

    @Test
    @DisplayName("unsuccess authorize")
    void testUnauthorizedAccess() throws Exception {
        ParametersRequestDto payload = new ParametersRequestDto(1, 1, "TEST", 1, 1, null);
        String jsonPayload = mapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + testPort + "/api/parametres"))
                .header("Content-Type", "application/json")
                .header("X-Auth-User", "q1")
                .header("X-Auth-Password", "wrong_password")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(401, response.statusCode(), "server must request 401 Unauthorized");
    }
}