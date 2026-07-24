package ru.itone.illya4gurenko.file_generation;

import java.io.IOException;
import java.time.LocalDateTime;

public interface FileGenerator {
    void generateFile(int codeBank, int codeFilial, String nameAES, int countRecords, int countFiles) throws IOException;
    void generateFile(int codeBank, int codeFilial, String nameAES, int countRecords, int countFiles, LocalDateTime inTime) throws IOException;

}
