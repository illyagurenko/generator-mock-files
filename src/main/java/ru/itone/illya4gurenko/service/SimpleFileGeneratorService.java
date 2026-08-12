package ru.itone.illya4gurenko.service;

import ru.itone.illya4gurenko.config.Base;
import ru.itone.illya4gurenko.struct_file.Footer;
import ru.itone.illya4gurenko.struct_file.Header;
import ru.itone.illya4gurenko.struct_file.TitleFile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Главный оркестратор генерации банковских реестров.
 * <p>
 * Генерирует валидные и намеренно поврежденные (invalid) файлы с заголовочной строкой
 * </p>
 * После завершения записи файла на диск автоматически делегирует отправку соответствующему
 * сервису-реализации {@link FileSender} на основе настроек приложения.
 */
public class SimpleFileGeneratorService extends Base implements FileGenerator {
    private static final SimpleFileGeneratorService INSTANCE = new SimpleFileGeneratorService();
    private final DataGenerator generator;
    private final Path outputDir;
    private final Charset charset;

    private final int percentageInvalid;

    private SimpleFileGeneratorService() {
        this.generator = DataFakerGeneratorService.getInstance();
        this.outputDir = config.getFileOut();
        this.charset = config.getFileCharset();
        this.percentageInvalid = config.getPercentageInvalid();

        info("init SimpleFileGeneratorService, save dir: {}, charset: {}, percentageInvalid: {}", outputDir, charset, percentageInvalid);
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            error("error creating output directory: {}", outputDir, e);
            throw new RuntimeException("error creating output dir: " + outputDir, e);
        }
    }

    public static SimpleFileGeneratorService getInstance() {
        return INSTANCE;
    }

    @Override
    public void generateFile(int codeBank, int codeFilial, String nameAES, int countRecords, int countFiles) throws IOException {
        generateFile(codeBank, codeFilial, nameAES, countRecords, countFiles, null);
    }

    @Override
    public void generateFile(int codeBank, int codeFilial, String nameAES, int countRecords, int countFiles, LocalDateTime inTime) throws IOException {
        info("generating files (inTime: {}), countFiles: {}, countRecords: {}, bank: {}, filial: {}",
                inTime, countFiles, countRecords, codeBank, codeFilial);

        for (int i = 0; i < countFiles; i++) {
            TitleFile titleFile = new TitleFile(codeBank, codeFilial, nameAES);
            Path path = outputDir.resolve(titleFile.toString());
            boolean isInvalid = ThreadLocalRandom.current().nextInt(100) < percentageInvalid;
            if (isInvalid) {
                generateInvalidFile(path, inTime, countRecords);
            } else {
                generateValidFile(path, inTime, countRecords);
            }
        }
        info("file generation completed");
    }

    /**
     * Создает корректный файл банковского реестра.
     *
     * @param path         Целевой путь файла
     * @param inTime       Плановое время исполнения
     * @param countRecords Количество строк клиентов
     * @throws IOException При ошибке записи или сетевой передачи
     */
    public void generateValidFile(Path path, LocalDateTime inTime, int countRecords) throws IOException {
        debug("writing valid file -> {}", path.getFileName());

        try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
            Header header = (inTime != null) ? new Header(inTime.toLocalDate(), inTime.toLocalTime()) : new Header();

            writer.write(header.toString());
            writer.newLine();

            for (int j = 0; j < countRecords; j++) {
                writer.write(generator.generateData());
                writer.newLine();
            }

            Footer footer = new Footer(countRecords);
            writer.write(footer.toString());
            debug("file successfully created: {}", path.getFileName());

        } catch (IOException e) {
            error("error writing file {}", path, e);
            throw new IOException("error generating file", e);
        }
        sendFileIfEnabled(path);
    }

    /**
     * Генерирует файл с внесенными повреждениями (для негативного тестирования приемников).
     *
     * @param path         Целевой путь файла
     * @param inTime       Плановое время исполнения
     * @param countRecords Количество строк клиентов
     * @throws IOException При ошибке записи или сетевой передачи
     */
    private void generateInvalidFile(Path path, LocalDateTime inTime, int countRecords) throws IOException {
        int corruptionType = ThreadLocalRandom.current().nextInt(4);
        final int noValidHeader = 0;
        final int noValidCountRecords = 1;
        final int noValidFooter = 2;
        final int noValidClientRecord = 3;


        try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
            Header header = (inTime != null) ? new Header(inTime.toLocalDate(), inTime.toLocalTime()) : new Header();
            Footer footer = new Footer(countRecords);

            switch (corruptionType) {
                case noValidHeader:
                    warn("bad header in {}", path.getFileName());
                    writer.write("header invalid");
                    writer.newLine();
                    for (int j = 0; j < countRecords; j++) {
                        writer.write(generator.generateData());
                        writer.newLine();
                    }
                    writer.write(footer.toString());
                    break;

                case noValidCountRecords:
                    warn("invalid count records in {}", path.getFileName());
                    writer.write(header.toString());
                    writer.newLine();
                    int actualRecords = Math.max(0, countRecords - 3);
                    for (int j = 0; j < actualRecords; j++) {
                        writer.write(generator.generateData());
                        writer.newLine();
                    }
                    writer.write(footer.toString());
                    break;

                case noValidFooter:
                    warn("bad footer in {}", path.getFileName());
                    writer.write(header.toString());
                    writer.newLine();
                    for (int j = 0; j < countRecords; j++) {
                        writer.write(generator.generateData());
                        writer.newLine();
                    }
                    writer.write("footer invalid");
                    break;

                case noValidClientRecord:
                    warn("bad records in {}", path.getFileName());
                    writer.write(header.toString());
                    writer.newLine();
                    for (int j = 0; j < countRecords/2; j++) {
                        writer.write(generator.generateData());
                        writer.newLine();
                    }
                    writer.write("bad client record");
                    writer.newLine();
                    for (int j = 0; j < (countRecords - (countRecords / 2) - 1); j++) {
                        writer.write(generator.generateData());
                        writer.newLine();
                    }
                    writer.write(footer.toString());
                    break;
                default:
                    throw new NumberFormatException("Error ThreadLocalRandom");
            }

            debug("invalid file successfully written: {}", path.getFileName());

        } catch (IOException e) {
            error("failed to write invalid file: {}", path, e);
            throw new IOException("failed to write invalid file", e);
        }
        sendFileIfEnabled(path);
    }

    /**
     * Выбирает реализацию {@link FileSender} на основе флагов конфигурации
     * и отправляет файл в соответствующий канал.
     *
     * @param path Путь к сформированному файлу
     * @throws IOException При ошибках передачи
     */
    private void sendFileIfEnabled(Path path) throws IOException {
        if (config.isSendGrpcEnabled()) {
            info("Sending file via gRPC stream...");
            getGrpcSenderService().sendFile(path, config.getGrpcReceiverUrl());
        } else if (config.isSendMultipartEnabled()) {
            info("Sending file via HTTP Multipart...");
            getMultipartSenderService().sendFile(path, config.getReceiverUrl());
        } else if (config.isSendChunkedEnabled()) {
            info("Sending file via HTTP Chunked...");
            getChunkedFileSenderService().sendFile(path, config.getReceiverUrl());
        }
    }
}
