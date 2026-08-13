package ru.itone.illya4gurenko.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.itone.illya4gurenko.config.Base;
import ru.itone.illya4gurenko.dto.ParametersRequestDto;
import ru.itone.illya4gurenko.exception.VisitorTypeException;
import ru.itone.illya4gurenko.service.GenerateEnrollVisitor;
import ru.itone.illya4gurenko.service.Visitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Обработчик HTTP POST запросов для запуска процесса генерации файлов
 * <p>
 * <b>Сценарий работы:</b>
 * <ol>
 *   <li>Проверяет, что HTTP-метод — {@code POST} иначе возвращает HTTP 405.</li>
 *   <li>Проверяет авторизацию через заголовки {@code X-Auth-User} и {@code X-Auth-Password} иначе HTTP 401.</li>
 *   <li>Парсит JSON-тело запроса в {@link ParametersRequestDto} иначе HTTP 400.</li>
 *   <li>Валидирует параметры {@code countFiles} и {@code countRecords} > 0.</li>
 *   <li>Запускает генерацию файлов в <b>отдельном фоновом потоке</b> чтобы не блокировать ответ.</li>
 *   <li>Сразу возвращает клиенту ответ {@code 200 OK} о том, что задача принята.</li>
 * </ol>
 * </p>
 */
public class ParametersHandler extends Base implements HttpHandler {

    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);

    /**
     * Пул фоновых воркеров с фиксированным размером (например, 10 параллельных задач).
     * Создает демон-потоки с предсказуемыми именами для удобного чтения логов.
     */
    private static final ExecutorService fileGenExecutor = Executors.newFixedThreadPool(10, r -> {
        Thread thread = new Thread(r);
        thread.setName("file-gen-worker-" + THREAD_COUNTER.getAndIncrement());
        thread.setDaemon(true); // Демон-потоки не блокируют завершение приложения
        return thread;
    });

    public ParametersHandler() {
        info("ParametersHandler initialized");
    }

    /**
     * Главный метод обработки входящего HTTP-запроса от клиента.
     *
     * @param exchange Объект HTTP-взаимодействия содержит заголовки, тело запроса и поток ответа
     * @throws IOException В случае ошибок ввода-вывода при работе со связью
     */
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


            fileGenExecutor.submit(() -> {
                try {
                    debug("starting background file generation task via Visitor for client {}", remoteAddress);

                    getEnrollVisitor().visit(request);

                    info("background file generation finished successfully for client {}", remoteAddress);
                } catch (VisitorTypeException e) {
                    error("invalid object type passed to visitor", e);
                } catch (Exception e) {
                    error("error during background file generation for client {}", remoteAddress, e);
                }
            });

            sendResponse(exchange, 200, "request accepted, file generation started.");
            info("request from {} processed successfully, http 200 sent", remoteAddress);

        } catch (Error e) {
            error("unexpected internal error processing request from client {}", remoteAddress, e);
            sendResponse(exchange, 500, "internal server error");
        }
    }

    /**
     * Вспомогательный метод для отправки текстового HTTP-ответа клиенту.
     *
     * @param exchange     Объект HTTP-взаимодействия
     * @param statusCode   HTTP-код ответа
     * @param responseText Текст ответа
     * @throws IOException При ошибке записи в поток ответа
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}