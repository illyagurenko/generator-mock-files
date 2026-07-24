package ru.itone.illya4gurenko.file_generation;

import ru.itone.illya4gurenko.model.Footer;
import ru.itone.illya4gurenko.model.Header;
import ru.itone.illya4gurenko.model.TitleFile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class TxtGenerator implements FileGenerator {
    private final DataGenerator generator;

    public TxtGenerator(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void generateFile(int codeBank, int codeFilial, String nameAES, int countRecords, int countFiles) throws IOException {
        for (int i = 0; i < countFiles; i++) {
            TitleFile titleFile = new TitleFile(codeBank, codeFilial, nameAES);
            Path path = Paths.get(titleFile.toString() + ".txt");

            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                Header header = new Header();
                writer.write(header.toString());
                writer.newLine();
                for (int j = 0; j < countRecords; j++) {
                    writer.write(generator.generateData());
                    writer.newLine();
                }
                Footer footer = new Footer(countRecords);
                writer.write(footer.toString());

            } catch (IOException e) {
                throw new IOException("error generate file");
            }

        }
    }

    @Override
    public void generateFile(int codeBank, int codeFilial, String nameAES, int countRecords, int countFiles, LocalDateTime inTime) throws IOException {
        for (int i = 0; i < countFiles; i++) {
            TitleFile titleFile = new TitleFile(codeBank, codeFilial, nameAES);
            Path path = Paths.get(titleFile.toString() + ".txt");

            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                Header header = new Header(inTime.toLocalDate(), inTime.toLocalTime());
                writer.write(header.toString());
                writer.newLine();
                for (int j = 0; j < countRecords; j++) {
                    writer.write(generator.generateData());
                    writer.newLine();
                }
                Footer footer = new Footer(countRecords);
                writer.write(footer.toString());

            } catch (IOException e) {
                throw new IOException("error generate file");
            }

        }
    }

}
