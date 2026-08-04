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
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

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
    }

    private void generateInvalidFile(Path filePath, LocalDateTime inTime, int countRecords) throws IOException {
        int corruptionType = ThreadLocalRandom.current().nextInt(4);
        final int noValidHeader = 0;
        final int noValidCountRecords = 1;
        final int noValidFooter = 2;
        final int noValidClientRecord = 3;


        try (BufferedWriter writer = Files.newBufferedWriter(filePath, charset)) {
            Header header = (inTime != null) ? new Header(inTime.toLocalDate(), inTime.toLocalTime()) : new Header();
            Footer footer = new Footer(countRecords);

            switch (corruptionType) {
                case noValidHeader:
                    warn("bad header in {}", filePath.getFileName());
                    writer.write("header invalid");
                    writer.newLine();
                    for (int j = 0; j < countRecords; j++) {
                        writer.write(generator.generateData());
                        writer.newLine();
                    }
                    writer.write(footer.toString());
                    break;

                case noValidCountRecords:
                    warn("invalid count records in {}", filePath.getFileName());
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
                    warn("bad footer in {}", filePath.getFileName());
                    writer.write(header.toString());
                    writer.newLine();
                    for (int j = 0; j < countRecords; j++) {
                        writer.write(generator.generateData());
                        writer.newLine();
                    }
                    writer.write("footer invalid");
                    break;

                case noValidClientRecord:
                    warn("bad records in {}", filePath.getFileName());
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

            debug("invalid file successfully written: {}", filePath.getFileName());
        } catch (IOException e) {
            error("failed to write invalid file: {}", filePath, e);
            throw new IOException("failed to write invalid file", e);
        }
    }
}
