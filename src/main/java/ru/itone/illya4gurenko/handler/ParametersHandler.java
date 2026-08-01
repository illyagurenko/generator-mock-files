package ru.itone.illya4gurenko.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.itone.illya4gurenko.config.Base;
import ru.itone.illya4gurenko.dto.ParametersRequestDto;

import java.io.IOException;
import java.io.InputStream;

public class ParametersHandler extends Base implements HttpHandler {

    public ParametersHandler() {
        info("init ParametersHandler");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String remoteAddress = exchange.getRemoteAddress().toString();

            if (!"POST".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            Headers headers = exchange.getRequestHeaders();
            String authUser = headers.getFirst("X-Auth-User");
            String authPasswordEncrypted = headers.getFirst("X-Auth-Password");

            if (!getAuthService().authorize(authUser, authPasswordEncrypted)) {
                warn("unauthorized access attempt from: {}. User: {}", remoteAddress, authUser);
                exchange.sendResponseHeaders(401, -1);
                return;
            }

            ParametersRequestDto request;
            try (InputStream is = exchange.getRequestBody()) {
                request = objectMapper.readValue(is, ParametersRequestDto.class);
            }

            new Thread(() -> {
                try {
                    getFileGenerator().generateFile(
                            request.codeBank(),
                            request.codeFilial(),
                            request.nameAES(),
                            request.countRecords(),
                            request.countFiles(),
                            request.inTime() != null ? request.inTime() : java.time.LocalDateTime.now()
                    );
                } catch (Exception e) {
                    error("error during background file generation", e);
                }
            }).start();

            exchange.sendResponseHeaders(200, -1);

        } catch (Exception e) {
            error("request handling error", e);
            exchange.sendResponseHeaders(500, -1);
        }
    }
}