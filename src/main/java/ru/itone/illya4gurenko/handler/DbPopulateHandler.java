package ru.itone.illya4gurenko.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.itone.illya4gurenko.config.Base;
import ru.itone.illya4gurenko.dto.DbPopulateRequestDto;
import ru.itone.illya4gurenko.struct_file.GruVistaTab;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DbPopulateHandler extends Base implements HttpHandler {

    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);

    private static final ExecutorService dbPopulateExecutor = Executors.newFixedThreadPool(5, r -> {
        Thread thread = new Thread(r);
        thread.setName("db-populate-worker-" + THREAD_COUNTER.getAndIncrement());
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String remoteAddress = exchange.getRemoteAddress().toString();
        String method = exchange.getRequestMethod();

        debug("received DB populate request, method: {}, client: {}", method, remoteAddress);

        try {
            if (!"POST".equalsIgnoreCase(method)) {
                sendResponse(exchange, 405, "method not POST");
                return;
            }

            // Проверка авторизации
            Headers headers = exchange.getRequestHeaders();
            String authUser = headers.getFirst("X-Auth-User");
            String authPasswordEncrypted = headers.getFirst("X-Auth-Password");

            if (!getAuthService().authorize(authUser, authPasswordEncrypted)) {
                sendResponse(exchange, 401, "unauthorized access.");
                return;
            }

            DbPopulateRequestDto request;
            try (InputStream is = exchange.getRequestBody()) {
                request = objectMapper.readValue(is, DbPopulateRequestDto.class);
            } catch (JsonProcessingException e) {
                sendResponse(exchange, 400, "bad request: invalid json");
                return;
            }

            if (request == null || request.countRecords() <= 0) {
                sendResponse(exchange, 400, "bad request: countRecords must be > 0");
                return;
            }

            info("Starting async DB population. Client: {}, records to generate: {}", remoteAddress, request.countRecords());

            dbPopulateExecutor.submit(() -> {
                try {
                    List<GruVistaTab> records = new ArrayList<>(request.countRecords());
                    for (int i = 0; i < request.countRecords(); i++) {
                        records.add(getDataGenerator().generateGruVistaRecord());
                    }
                    getDatabaseService().insertGruVistaBatch(records);
                } catch (Exception e) {
                    error("Error during DB background population", e);
                }
            });

            sendResponse(exchange, 200, "request accepted, database population started in background.");

        } catch (Exception e) {
            error("unexpected internal error processing DB request", e);
            sendResponse(exchange, 500, "internal server error");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}