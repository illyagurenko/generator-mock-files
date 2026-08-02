package ru.itone.illya4gurenko.struct_file;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

public class TitleFile {
    private static final char FIRST_CHAR = 'Z';
    private static final char POINT = '.';
    private static final char LINE = '_';
    private static final String ENROLL = "ENROLL";

    private final int codeBank;
    private final int codeFilial;
    private final String nameAES;
    private final int julianDate;

    private final int number;
    private static final AtomicInteger NUMBER_COUNTER = new AtomicInteger(0);

    public TitleFile(int codeBank, int codeFilial, String nameAES) {
        LocalDate date = LocalDate.now();
        this.julianDate = date.getDayOfYear();
        this.codeBank = codeBank;
        this.codeFilial = codeFilial;
        this.nameAES = nameAES;
        this.number = NUMBER_COUNTER.incrementAndGet();
    }

    @Override
    public String toString() {
        return String.format("%1s%03d%03d%1s%s%1s%s%d%1s%03d",
                FIRST_CHAR, codeBank, codeFilial, POINT, nameAES, LINE, ENROLL, number, POINT, julianDate);
    }
}
