package ru.itone.illya4gurenko.service;

import ru.itone.illya4gurenko.config.Base;
import ru.itone.illya4gurenko.dto.ParametersRequestDto;
import ru.itone.illya4gurenko.exception.VisitorTypeException;
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
 * Главный генератор банковских реестров.
 * <p>
 * Генерирует валидные и намеренно поврежденные (invalid) файлы с заголовочной строкой
 * </p>
 * После завершения записи файла на диск автоматически делегирует отправку соответствующему
 * сервису-реализации {@link FileSender} на основе настроек приложения.
 */
public class GenerateEnrollVisitor extends Base implements Visitor {

    private static final GenerateEnrollVisitor INSTANCE = new GenerateEnrollVisitor();

    private static final int CORRUPTION_HEADER = 0;
    private static final int INVALID_COUNT_RECORDS = 1;
    private static final int CORRUPTION_FOOTER = 2;
    private static final int CORRUPTION_CLIENT_RECORDS = 3;
    private static final int INCORRECT_CHARSET = 4;
    private static final int EMPTY_FILE = 5;
    private static final int BLANK_FILE = 6;
    private static final int TOTAL_CORRUPTION_TYPES = 7;

    private final Path outputDir;
    private final Charset defaultCharset;
    private final int percentageInvalid;

    private GenerateEnrollVisitor() {
        this.outputDir = config.getFileOut();
        this.defaultCharset = config.getFileCharset();
        this.percentageInvalid = config.getPercentageInvalid();

        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            error("error creating output directory: {}", outputDir, e);
            throw new RuntimeException("error creating output dir: " + outputDir, e);
        }
    }

    public static GenerateEnrollVisitor getInstance() {
        return INSTANCE;
    }

    @Override
    public void visit(Object o) throws VisitorTypeException {
        if (!(o instanceof ParametersRequestDto request)) {
            throw new VisitorTypeException("Type error in Visitor: Expected ParametersRequestDto, but got " +
                    (o != null ? o.getClass().getName() : "null"));
        }

        info("Visitor generating files (inTime: {}), countFiles: {}, countRecords: {}, bank: {}, filial: {}",
                request.inTime(), request.countFiles(), request.countRecords(), request.codeBank(), request.codeFilial());

        for (int i = 0; i < request.countFiles(); i++) {
            TitleFile titleFile = new TitleFile(request.codeBank(), request.codeFilial(), request.nameAES());
            Path path = outputDir.resolve(titleFile.toString());
            boolean isInvalid = ThreadLocalRandom.current().nextInt(100) < percentageInvalid;

            try {
                if (isInvalid) {
                    generateInvalidFile(path, request.inTime(), request.countRecords());
                } else {
                    generateValidFile(path, request.inTime(), request.countRecords());
                }
            } catch (IOException e) {
                error("failed to process file generation in Visitor: {}", path, e);
                throw new RuntimeException("file generation error", e);
            }
        }
        info("Visitor file generation completed successfully");
    }

    private void generateValidFile(Path path, LocalDateTime inTime, int countRecords) throws IOException {
        debug("writing valid file via Visitor -> {}", path.getFileName());

        try (BufferedWriter writer = Files.newBufferedWriter(path, defaultCharset)) {
            Header header = (inTime != null) ? new Header(inTime.toLocalDate(), inTime.toLocalTime()) : new Header();

            writer.write(header.toString());
            writer.newLine();

            for (int j = 0; j < countRecords; j++) {
                writer.write(getDataGenerator().generateData());
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
     * Генерирует невалидный файл с использованием именованных сценариев сбоя.
     */
    private void generateInvalidFile(Path path, LocalDateTime inTime, int countRecords) throws IOException {
        int corruptionType = ThreadLocalRandom.current().nextInt(TOTAL_CORRUPTION_TYPES);

        Header header = (inTime != null) ? new Header(inTime.toLocalDate(), inTime.toLocalTime()) : new Header();
        Footer footer = new Footer(countRecords);

        switch (corruptionType) {
            case CORRUPTION_HEADER -> {
                warn("Bad header in {}", path.getFileName());
                try (BufferedWriter writer = Files.newBufferedWriter(path, defaultCharset)) {
                    writer.write("header invalid");
                    writer.newLine();
                    for (int j = 0; j < countRecords; j++) {
                        writer.write(getDataGenerator().generateData());
                        writer.newLine();
                    }
                    writer.write(footer.toString());
                }
            }
            case INVALID_COUNT_RECORDS -> {
                warn("Invalid count records in {}", path.getFileName());
                try (BufferedWriter writer = Files.newBufferedWriter(path, defaultCharset)) {
                    writer.write(header.toString());
                    writer.newLine();
                    int actualRecords = Math.max(0, countRecords - 3);
                    for (int j = 0; j < actualRecords; j++) {
                        writer.write(getDataGenerator().generateData());
                        writer.newLine();
                    }
                    writer.write(footer.toString());
                }
            }
            case CORRUPTION_FOOTER -> {
                warn("Bad footer in {}", path.getFileName());
                try (BufferedWriter writer = Files.newBufferedWriter(path, defaultCharset)) {
                    writer.write(header.toString());
                    writer.newLine();
                    for (int j = 0; j < countRecords; j++) {
                        writer.write(getDataGenerator().generateData());
                        writer.newLine();
                    }
                    writer.write("footer invalid");
                }
            }
            case CORRUPTION_CLIENT_RECORDS -> {
                warn("Bad client records in {}", path.getFileName());
                try (BufferedWriter writer = Files.newBufferedWriter(path, defaultCharset)) {
                    writer.write(header.toString());
                    writer.newLine();
                    for (int j = 0; j < countRecords / 2; j++) {
                        writer.write(getDataGenerator().generateData());
                        writer.newLine();
                    }
                    writer.write("bad client record");
                    writer.newLine();
                    for (int j = 0; j < (countRecords - (countRecords / 2) - 1); j++) {
                        writer.write(getDataGenerator().generateData());
                        writer.newLine();
                    }
                    writer.write(footer.toString());
                }
            }
            case INCORRECT_CHARSET -> {
                Charset badCharset = (ThreadLocalRandom.current().nextBoolean()) ?
                        Charset.forName("Windows-1251") : Charset.forName("KOI8-R");
                warn("Invalid encoding ({}) in {}", badCharset.name(), path.getFileName());

                try (BufferedWriter writer = Files.newBufferedWriter(path, badCharset)) {
                    writer.write(header.toString());
                    writer.newLine();
                    for (int j = 0; j < countRecords; j++) {
                        writer.write(getDataGenerator().generateData());
                        writer.newLine();
                    }
                    writer.write(footer.toString());
                }
            }
            case EMPTY_FILE -> {
                warn("Empty file (0 bytes) generated at {}", path.getFileName());
                Files.write(path, new byte[0]);
            }
            case BLANK_FILE -> {
                warn("Spaces and newlines only in {}", path.getFileName());
                try (BufferedWriter writer = Files.newBufferedWriter(path, defaultCharset)) {
                    writer.write("          \n");
                    writer.write("\r\n");
                    writer.write("\t\t\t\t\t\r\n");
                    writer.write("     ");
                    writer.newLine();
                    writer.write("\r\n\n\r\n");
                }
            }
            default -> error("invalid corruption type: {}", corruptionType);
        }

        debug("invalid file successfully written: {}", path.getFileName());
        sendFileIfEnabled(path);
    }

    private void sendFileIfEnabled(Path path) throws IOException {
        if (config.isSendGrpcEnabled()) {
            info("Visitor: Sending file via gRPC stream");
            getGrpcSenderService().sendFile(path, config.getGrpcReceiverUrl());
        } else if (config.isSendMultipartEnabled()) {
            info("Visitor: Sending file via HTTP Multipart");
            getMultipartSenderService().sendFile(path, config.getReceiverUrl());
        } else if (config.isSendChunkedEnabled()) {
            info("Visitor: Sending file via HTTP Chunked");
            getChunkedFileSenderService().sendFile(path, config.getReceiverUrl());
        }
    }
}