package ru.itone.illya4gurenko.dto_files;

import java.time.LocalDate;

public class TitleFile {
    private static final char FIRST_CHAR = 'Z';
    private static final char POINT = '.';
    private static final char LINE = '_';
    private static final String ENROLL = "ENROLL";

    private final int codeBank;
    private final int codeFilial;
    private final String nameAES;
    private final int julianDate;

    private static int number;

    public TitleFile(int codeBank, int codeFilial, String nameAES) {
        LocalDate date = LocalDate.now();
        this.julianDate = date.getDayOfYear();
        this.codeBank = codeBank;
        this.codeFilial = codeFilial;
        this.nameAES = nameAES;
        number++;
    }

    @Override
    public String toString() {
        return String.format("%1s%3d%3d%1s" + nameAES + "%1s%6s" + number + "%1s%3d",
                FIRST_CHAR, codeBank, codeFilial, POINT, LINE, ENROLL, POINT, julianDate);
    }
}
