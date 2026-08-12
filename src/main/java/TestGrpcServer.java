
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import ru.itone.illya4gurenko.grpc.FileChunk;
import ru.itone.illya4gurenko.grpc.FileUploadServiceGrpc;
import ru.itone.illya4gurenko.grpc.UploadStatus;

import java.io.IOException;

/**
 * Вспомогательный встраиваемый gRPC-сервер для локальной отладки и тестирования.
 * <p>
 * Принимает входящие потоки байтовых чанков файлов Client Streaming по протоколу HTTP/2,
 * подсчитывает размер принятых данных и выводит результаты обработки в консоль.
 * </p>
 */
public class TestGrpcServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 9090;

        // локальный gRPC сервер на 9090
        Server server = ServerBuilder.forPort(port)
                .addService(new FileUploadServiceImpl())
                .build()
                .start();

        System.out.println("test grpc server on port " + port);

        server.awaitTermination();
    }

    /**
     * Внутренняя реализация сгенерированного gRPC-сервиса приема файлов.
     */
    static class FileUploadServiceImpl extends FileUploadServiceGrpc.FileUploadServiceImplBase {

        /**
         * Обрабатывает входящий поток байтовых чанков файла от клиента.
         *
         * @param responseObserver Наблюдатель для отправки итогового статуса клиенту
         * @return Наблюдатель для приема входящих чанков {@link FileChunk}
         */
        @Override
        public StreamObserver<FileChunk> uploadFile(StreamObserver<UploadStatus> responseObserver) {
            return new StreamObserver<>() {
                private int totalBytes = 0;
                private String fileName = "";

                @Override
                public void onNext(FileChunk chunk) {
                    totalBytes += chunk.getContent().size();
                    fileName = chunk.getFileName();
                    System.out.println("--> Получен чанк размером: " + chunk.getContent().size() + " байт");
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Ошибка на сервере: " + t.getMessage());
                }

                @Override
                public void onCompleted() {

                    System.out.println("file [" + fileName + "] success");
                    System.out.println("all bytes: " + totalBytes);


                    UploadStatus status = UploadStatus.newBuilder()
                            .setIsSuccess(true)
                            .setMessage("file save on server")
                            .setBytesReceived(totalBytes)
                            .build();

                    responseObserver.onNext(status);
                    responseObserver.onCompleted();
                }
            };
        }
    }
}