package ru.itone.illya4gurenko.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.itone.illya4gurenko.config.Base;
import ru.itone.illya4gurenko.dto.ParametersRequestDto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class ParametersHandler extends Base implements HttpHandler {

    public ParametersHandler() {
        info("ParametersHandler initialized");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String remoteAddress = exchange.getRemoteAddress().toString();
        String method = exchange.getRequestMethod();

        debug("received http request, method: {}, client: {}, uri: {}", method, remoteAddress, exchange.getRequestURI());

        try {
            if (!"POST".equalsIgnoreCase(method)) {
                warn("uncorrected http method: {} from client {}, expected POST", method, remoteAddress);
                sendResponse(exchange, 405, "method not POST");
                return;
            }

            Headers headers = exchange.getRequestHeaders();
            String authUser = headers.getFirst("X-Auth-User");
            String authPasswordEncrypted = headers.getFirst("X-Auth-Password");

            if (!getAuthService().authorize(authUser, authPasswordEncrypted)) {
                warn("unauthorized access attempt from client: {}. user: {}", remoteAddress, authUser);
                sendResponse(exchange, 401, "unauthorized access.");
                return;
            }

            ParametersRequestDto request;
            try (InputStream is = exchange.getRequestBody()) {
                request = objectMapper.readValue(is, ParametersRequestDto.class);
            } catch (JsonProcessingException e) {
                warn("invalid json from client {}: {}", remoteAddress, e.getMessage());
                sendResponse(exchange, 400, "bad request:, invalid json");
                return;
            } catch (IOException e) {
                error("error reading request body from client {}", remoteAddress, e);
                sendResponse(exchange, 400, "bad request: unread request body");
                return;
            }

            if (request == null || request.countFiles() <= 0 || request.countRecords() <= 0) {
                warn("validation failed for client {}, request: {}", remoteAddress, request);
                sendResponse(exchange, 400, "bad request: countFiles and countRecords must be better 0");
                return;
            }

            info("successfully authenticated and parsed json from {}, bank: {}, files: {}, records: {}",
                    remoteAddress, request.codeBank(), request.countFiles(), request.countRecords());


            new Thread(() -> {
                try {
                    debug("starting background file generation task for client {}", remoteAddress);
                    getFileGenerator().generateFile(
                            request.codeBank(),
                            request.codeFilial(),
                            request.nameAES(),
                            request.countRecords(),
                            request.countFiles(),
                            request.inTime()
                    );
                    info("background file generation finished successfully for client {}", remoteAddress);
                } catch (Exception e) {
                    error("error during background file generation for client {}", remoteAddress, e);
                }
            }, "file-gen-" + System.currentTimeMillis()).start();

            sendResponse(exchange, 200, "request accepted, file generation started.");
            info("request from {} processed successfully, http 200 sent", remoteAddress);

        } catch (Error e) {
            error("unexpected internal error processing request from client {}", remoteAddress, e);
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