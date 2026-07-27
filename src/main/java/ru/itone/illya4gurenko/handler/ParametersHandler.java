package ru.itone.illya4gurenko.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.itone.illya4gurenko.dto.ParametersRequest;
import ru.itone.illya4gurenko.file_generation.FileGenerator;


import java.io.IOException;
import java.io.InputStream;


public class ParametersHandler implements HttpHandler {
    private final FileGenerator fileGenerator;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public ParametersHandler(FileGenerator fileGenerator) {
        this.fileGenerator = fileGenerator;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        try (InputStream is = exchange.getRequestBody()) {
            ParametersRequest request = mapper.readValue(is, ParametersRequest.class);
            try {
                if(request.inTime() == null){
                    fileGenerator.generateFile(
                            request.codeBank(),
                            request.codeFilial(),
                            request.nameAES(),
                            request.countRecords(),
                            request.countFiles());
                }
                else{
                    fileGenerator.generateFile(
                            request.codeBank(),
                            request.codeFilial(),
                            request.nameAES(),
                            request.countRecords(),
                            request.countFiles(),
                            request.inTime());
                }

            } catch (IOException e) {
                throw new RuntimeException("generate file error", e);
            }

            exchange.sendResponseHeaders(200, -1);
        }
    }
}
