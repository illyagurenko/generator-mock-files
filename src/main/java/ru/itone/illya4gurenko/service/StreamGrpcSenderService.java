package ru.itone.illya4gurenko.service;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import ru.itone.illya4gurenko.config.Base;
import ru.itone.illya4gurenko.grpc.FileChunk;
import ru.itone.illya4gurenko.grpc.FileUploadServiceGrpc;
import ru.itone.illya4gurenko.grpc.UploadStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Сервис высокопроизводительной потоковой отправки файлов по протоколу <b>gRPC (Client Streaming)</b>.
 * <p>
 * Читает файл с диска порциями по 64 Килобайта и непрерывно передает их по HTTP/2 сокету
 * на gRPC-сервер. По завершении ожидает итоговый статус от сервера.
 * </p>
 */
public class StreamGrpcSenderService extends Base implements FileSender{

    private static final StreamGrpcSenderService INSTANCE = new StreamGrpcSenderService();
    private static final int CHUNK_SIZE = 64 * 1024;

    private StreamGrpcSenderService() {}

    public static StreamGrpcSenderService getInstance() {
        return INSTANCE;
    }

    /**
     * Отправляет файл на gRPC-сервер через клиентский стриминг чанков байт.
     *
     * @param filePath  Путь к передаваемому файлу
     * @param targetUrl Адрес целевого gRPC-сервера в формате host:port
     * @throws IOException При ошибках чтения файла или таймауте gRPC-соединения
     */
    @Override
    public void sendFile(Path filePath, String targetUrl) throws IOException {
        String fileName = filePath.getFileName().toString();


        String cleanTarget = targetUrl
                .replaceAll("^grpc://", "")
                .replaceAll("^https?://", "");

        info("Starting gRPC byte streaming for: {} to target: {}", fileName, cleanTarget);

        if (!Files.exists(filePath)) {
            error("File not found: {}", filePath);
            return;
        }

        ManagedChannel channel = ManagedChannelBuilder.forTarget(cleanTarget)
                .usePlaintext()
                .build();

        FileUploadServiceGrpc.FileUploadServiceStub asyncStub = FileUploadServiceGrpc.newStub(channel);
        CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<UploadStatus> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(UploadStatus status) {
                if (status.getIsSuccess()) {
                    info("gRPC stream upload SUCCESS! Message: {}, Bytes received: {}",
                            status.getMessage(), status.getBytesReceived());
                } else {
                    error("gRPC stream upload FAILED! Message: {}", status.getMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                error("Error received from gRPC server during streaming", t);
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                info("gRPC stream closed successfully by server");
                finishLatch.countDown();
            }
        };

        StreamObserver<FileChunk> requestStream = asyncStub.uploadFile(responseObserver);

        try (InputStream inputStream = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                FileChunk chunk = FileChunk.newBuilder()
                        .setFileName(fileName)
                        .setContent(ByteString.copyFrom(buffer, 0, bytesRead))
                        .build();

                requestStream.onNext(chunk);
            }

            requestStream.onCompleted();

            if (!finishLatch.await(1, TimeUnit.MINUTES)) {
                warn("gRPC streaming timeout reached");
            }

        } catch (Exception e) {
            error("Error while streaming byte chunks", e);
            requestStream.onError(e);
        } finally {
            channel.shutdown();
        }
    }
}