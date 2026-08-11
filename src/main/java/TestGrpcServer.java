
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

// Импортируем сгенерированные Protobuf классы:
import ru.itone.illya4gurenko.grpc.FileChunk;
import ru.itone.illya4gurenko.grpc.FileUploadServiceGrpc;
import ru.itone.illya4gurenko.grpc.UploadStatus;

import java.io.IOException;

public class TestGrpcServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 9090;

        // Запускаем локальный gRPC сервер на порту 9090
        Server server = ServerBuilder.forPort(port)
                .addService(new FileUploadServiceImpl())
                .build()
                .start();


        System.out.println("test grpc server on port " + port);


        // Держим сервер запущенным
        server.awaitTermination();
    }

    // Реализация сервиса приема
    static class FileUploadServiceImpl extends FileUploadServiceGrpc.FileUploadServiceImplBase {

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