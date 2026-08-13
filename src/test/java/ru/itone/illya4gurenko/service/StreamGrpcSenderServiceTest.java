package ru.itone.illya4gurenko.service;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.*;
import ru.itone.illya4gurenko.grpc.FileChunk;
import ru.itone.illya4gurenko.grpc.FileUploadServiceGrpc;
import ru.itone.illya4gurenko.grpc.UploadStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("test grpc transfer ")
class StreamGrpcSenderServiceTest {

    private Server mockGrpcServer;
    private final int testGrpcPort = 9099;
    private Path tempFile;
    private final AtomicInteger totalBytesReceived = new AtomicInteger(0);
    private final AtomicReference<String> receivedFileName = new AtomicReference<>("");

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("grpc-test-", ".txt");
        String content = "H 20260101 IMMEDIATE\nGRPC CLIENT STREAMING TEST DATA\nT 1";
        Files.writeString(tempFile, content);

        // Запускаем локальный встраиваемый gRPC сервер
        mockGrpcServer = ServerBuilder.forPort(testGrpcPort)
                .addService(new FileUploadServiceGrpc.FileUploadServiceImplBase() {
                    @Override
                    public StreamObserver<FileChunk> uploadFile(StreamObserver<UploadStatus> responseObserver) {
                        return new StreamObserver<>() {
                            @Override
                            public void onNext(FileChunk chunk) {
                                totalBytesReceived.addAndGet(chunk.getContent().size());
                                receivedFileName.set(chunk.getFileName());
                            }

                            @Override
                            public void onError(Throwable t) {}

                            @Override
                            public void onCompleted() {
                                UploadStatus status = UploadStatus.newBuilder()
                                        .setIsSuccess(true)
                                        .setMessage("OK")
                                        .setBytesReceived(totalBytesReceived.get())
                                        .build();
                                responseObserver.onNext(status);
                                responseObserver.onCompleted();
                            }
                        };
                    }
                })
                .build()
                .start();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (mockGrpcServer != null) {
            mockGrpcServer.shutdownNow();
        }
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("success grps transfer gRPC Client Streaming")
    void testSendFileStreamSuccess() throws IOException {
        String targetGrpcUrl = "localhost:" + testGrpcPort;
        long expectedFileSize = Files.size(tempFile);

        assertDoesNotThrow(() -> {
            StreamGrpcSenderService.getInstance().sendFile(tempFile, targetGrpcUrl);
        });

        assertEquals(expectedFileSize, totalBytesReceived.get(),
                "Количество полученных gRPC-сервером байт должно строго совпадать с размером файла");
        assertEquals(tempFile.getFileName().toString(), receivedFileName.get(),
                "Имя файла в gRPC чанке должно совпадать с переданным");
    }
}
