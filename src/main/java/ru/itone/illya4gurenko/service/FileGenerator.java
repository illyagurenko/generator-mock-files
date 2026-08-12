package ru.itone.illya4gurenko.service;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Стратегия генератора файлов
 */
public interface FileGenerator {
    /**
     * Генерирует реестры с режимом немедленной обработки IMMEDIATE.
     *
     * @param codeBank     Код банка
     * @param codeFilial   Код филиала
     * @param nameAES      Имя АЭС
     * @param countRecords Количество записей клиентов в каждом файле
     * @param countFiles   Количество создаваемых файлов
     * @throws IOException При ошибке записи файлов на диск
     */
    void generateFile(int codeBank, int codeFilial, String nameAES, int countRecords, int countFiles) throws IOException;

    /**
     * Генерирует реестры с запланированным временем исполнения INTIME.
     *
     * @param codeBank     Код банка
     * @param codeFilial   Код филиала
     * @param nameAES      Имя АЭС
     * @param countRecords Количество записей клиентов в каждом файле
     * @param countFiles   Количество создаваемых файлов
     * @param inTime       Плановая дата и время обработки файла
     * @throws IOException При ошибке записи файлов на диск
     */
    void generateFile(int codeBank, int codeFilial, String nameAES, int countRecords, int countFiles, LocalDateTime inTime) throws IOException;

}
