package ru.itone.illya4gurenko.struct_file;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Класс структуры названия генерируемого файла.
 * Создается с фиксированной шириной
 * <p>
 * Формирует имя файла согласно шаблону:
 * {@code Z001002.GPB_ENROLL1.298}
 */

public class TitleFile {

    /** Первый символ имени файла */
    private static final char FIRST_CHAR = 'Z';

    /** Символ точки */
    private static final char POINT = '.';

    /** Символ подчеркивания */
    private static final char LINE = '_';

    /** Тип операции */
    private static final String ENROLL = "ENROLL";

    /** Потокобезопасный счетчик порядковых номеров файлов */
    private static final AtomicInteger NUMBER_COUNTER = new AtomicInteger(0);

    /** Код банка */
    private final int codeBank;

    /** Код филиала */
    private final int codeFilial;

    /** Наименование автоматизированной системы */
    private final String nameAES;

    /** День года по юлианскому календарю */
    private final int julianDate;

    /** Порядковый номер файла в рамках сессии */
    private final int number;

    /**
     * Конструктор генератора имени файла.
     * Автоматически вычисляет текущий юлианский день и инкрементирует порядковый номер.
     *
     * @param codeBank   Числовой код банка
     * @param codeFilial Числовой код филиала
     * @param nameAES    Имя автоматизированной системы
     */
    public TitleFile(int codeBank, int codeFilial, String nameAES) {
        LocalDate date = LocalDate.now();
        this.julianDate = date.getDayOfYear();
        this.codeBank = codeBank;
        this.codeFilial = codeFilial;
        this.nameAES = nameAES;
        this.number = NUMBER_COUNTER.incrementAndGet();
    }

    /**
     * Преобразует параметры в итоговое имя файла.
     *
     * @return Сформированное имя файла
     */
    @Override
    public String toString() {
        return String.format("%1s%03d%03d%1s%s%1s%s%d%1s%03d",
                FIRST_CHAR, codeBank, codeFilial, POINT, nameAES, LINE, ENROLL, number, POINT, julianDate);
    }
}