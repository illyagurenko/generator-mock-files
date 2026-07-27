package ru.itone.illya4gurenko.file_generation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.itone.illya4gurenko.model.Footer;
import ru.itone.illya4gurenko.model.Header;
import ru.itone.illya4gurenko.model.TitleFile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class SimpleFileGeneratorService implements FileGenerator {
    private static final Logger log = LoggerFactory.getLogger(SimpleFileGeneratorService.class);
    private final DataGenerator generator;
    private final Path outputDir;
    private final Charset charset;

    public SimpleFileGeneratorService(DataGenerator generator, Path outputDir, Charset charset) {
        this.generator = generator;
        this.outputDir = outputDir;
        this.charset = charset;
        log.info("init SimpleFileGeneratorService, save dir: {}, charset: {}", outputDir, charset);
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            log.error("error create dir {}", outputDir, e);
            throw new RuntimeException("error create dir " + outputDir, e);
        }
    }

    @Override
    public void generateFile(int codeBank, int codeFilial, String nameAES, int countRecords, int countFiles) throws IOException {
        log.info("generate file without intime parameters, count files: {}, records in file: {}, bank: {}, filial: {}",
                countFiles, countRecords, codeBank, codeFilial);
        for (int i = 0; i < countFiles; i++) {
            TitleFile titleFile = new TitleFile(codeBank, codeFilial, nameAES);
            Path path = outputDir.resolve(titleFile.toString());
            log.debug("start write in file [{}/{}] -> {}", (i + 1), countFiles, path.getFileName());

            try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
                Header header = new Header();
                writer.write(header.toString());
                writer.newLine();
                for (int j = 0; j < countRecords; j++) {
                    writer.write(generator.generateData());
                    writer.newLine();
                }
                Footer footer = new Footer(countRecords);
                writer.write(footer.toString());
                log.debug("file {} success write, count records: {}", path.getFileName(), countRecords);
            } catch (IOException e) {
                log.error("error generate file {}", path, e);
                throw new IOException("error generate file");
            }
        }
        log.info("generate files ending");

    }

    @Override
    public void generateFile(int codeBank, int codeFilial, String nameAES, int countRecords, int countFiles, LocalDateTime inTime) throws IOException {
        log.info("generate file with intime parameters: {}, count files: {}, records in file: {}, bank: {}, filial: {}",
                inTime, countFiles, countRecords, codeBank, codeFilial);
        for (int i = 0; i < countFiles; i++) {
            TitleFile titleFile = new TitleFile(codeBank, codeFilial, nameAES);
            Path path = outputDir.resolve(titleFile.toString());
            log.debug("start write in file with intime [{}/{}] -> {}", (i + 1), countFiles, path.getFileName());
            try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
                Header header = new Header(inTime.toLocalDate(), inTime.toLocalTime());
                writer.write(header.toString());
                writer.newLine();
                for (int j = 0; j < countRecords; j++) {
                    writer.write(generator.generateData());
                    writer.newLine();
                }
                Footer footer = new Footer(countRecords);
                writer.write(footer.toString());
                log.debug("file with intime {} success write, count records: {}", path.getFileName(), countRecords);
            } catch (IOException e) {
                log.error("error generate intime file  {}", path, e);
                throw new IOException("error generate file");
            }
        }
        log.info("generate intime files ending");
    }

}
