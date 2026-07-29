package ru.itone.illya4gurenko.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itone.illya4gurenko.dto.ParametersRequestDto;
import ru.itone.illya4gurenko.security.AuthService;
import ru.itone.illya4gurenko.service.FileGenerator;

import java.io.IOException;
import java.io.InputStream;


public class ParametersHandler implements HttpHandler {
    private static final Logger log = LoggerFactory.getLogger(ParametersHandler.class);
    private final FileGenerator fileGenerator;
    private final AuthService authService;


    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public ParametersHandler(FileGenerator fileGenerator, AuthService authService) {
        this.fileGenerator = fileGenerator;
        this.authService = authService;
        log.info("init post handler");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String remoteAddress = exchange.getRemoteAddress().toString();
        log.debug("get http-request. method: {}, client: {}, uri: {}", method, remoteAddress, exchange.getRequestURI());

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            log.info("uncorrected http method: {} by client {}, must be post", method, remoteAddress);
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Headers headers = exchange.getRequestHeaders();
        String authUser = headers.getFirst("X-Auth-User");
        String authPasswordEncrypted = headers.getFirst("X-Auth-Password");

        if (!authService.authorize(authUser, authPasswordEncrypted)) {
            log.warn("Unauthorized access attempt from client: {}. User: {}", remoteAddress, authUser);
            exchange.sendResponseHeaders(401, -1);
            return;
        }
        try (InputStream is = exchange.getRequestBody()) {
            ParametersRequestDto request = mapper.readValue(is, ParametersRequestDto.class);
            log.info("success auth and read JSON-body by {}, bank: {}, count files: {}, count records: {}",
                    remoteAddress, request.codeBank(), request.countFiles(), request.countRecords());
            try {
                if (request.inTime() == null) {
                    log.debug("generate file without intime parameter");
                    fileGenerator.generateFile(
                            request.codeBank(),
                            request.codeFilial(),
                            request.nameAES(),
                            request.countRecords(),
                            request.countFiles());
                } else {
                    log.debug("generate file with intime parameter");
                    fileGenerator.generateFile(
                            request.codeBank(),
                            request.codeFilial(),
                            request.nameAES(),
                            request.countRecords(),
                            request.countFiles(),
                            request.inTime());
                }

            } catch (IOException e) {
                log.error("generate file error for client: {}", remoteAddress, e);
                throw new RuntimeException("generate file error", e);
            }
            log.info("generate for {} success ending, http 200.", remoteAddress);
            exchange.sendResponseHeaders(200, -1);
        }
    }
}
