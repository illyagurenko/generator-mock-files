package ru.itone.illya4gurenko.struct_file;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Класс структуры заголовка генерируемого файла.
 * Создается с фиксированной шириной
 */
public class Header {
    /** Идентификатор заголовка */
    private static final char HEADER = 'H';

    /** Форматтер даты в формате yyyyMMdd*/
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** Форматтер времени в формате HHmmss*/
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

    /** Разделитель полей*/
    private static final char FILLER = ' ';

    /** Тип обработки файла: IMMEDIATE - сразу, INTIME - к определенному времени */
    private final ProcType procType;

    /** Текущая дата создания заголовка */
    private final LocalDate currentDate;

    /** Текущее время создания заголовка */
    private final LocalTime currentTime;

    /** Запланированная дата генерации */
    private final LocalDate inTimeDate;

    /** Запланированное время гкнерации */
    private final LocalTime inTimeTime;

    /**
     * Конструктор заголовка для типа {@link ProcType#INTIME}
     *
     * @param inTimeDate Запланированная дата генерации
     * @param inTimeTime Запланированное время генерации
     */
    public Header(LocalDate inTimeDate, LocalTime inTimeTime) {
        this.procType = ProcType.INTIME;
        this.currentDate = LocalDate.now();
        this.currentTime = LocalTime.now();
        this.inTimeDate = inTimeDate;
        this.inTimeTime = inTimeTime;
    }

    /**
     * Конструктор заголовка для типа {@link ProcType#IMMEDIATE}.
     */
    public Header() {
        this.procType = ProcType.IMMEDIATE;
        this.currentDate = LocalDate.now();
        this.currentTime = LocalTime.now();
        this.inTimeDate = null;
        this.inTimeTime = null;
    }

    /**
     * Преобразует объект заголовка в строку фиксированной ширины согласно шаблону.
     * <p>Для типа {@link ProcType#IMMEDIATE} формирует строку с текущей датой, временем и типом.</p>
     * <p>Для типа {@link ProcType#INTIME} дополнительно дописывает плановую дату и время исполнения.</p>
     * @return Форматированная заголовочная строка
     */
    @Override
    public String toString() {
        if (procType == ProcType.IMMEDIATE) {
            return String.format("%1s%1s%8s%1s%6s%1s%-9s",
                    HEADER,
                    FILLER,
                    currentDate.format(DATE_FORMATTER),
                    FILLER,
                    currentTime.format(TIME_FORMATTER),
                    FILLER,
                    procType
            );
        } else {
            return String.format("%1s%1s%8s%1s%6s%1s%6s%1s%8s%1s%6s",
                    HEADER,
                    FILLER,
                    currentDate.format(DATE_FORMATTER),
                    FILLER,
                    currentTime.format(TIME_FORMATTER),
                    FILLER,
                    procType,
                    FILLER,
                    inTimeDate.format(DATE_FORMATTER),
                    FILLER,
                    inTimeTime.format(TIME_FORMATTER)
            );
        }
    }
}