package ru.itone.illya4gurenko.model;

import java.time.LocalDate;

public class TitleFile {
    private static final char firstChar = 'Z';
    private static final char point = '.';
    private static final char line = '_';
    private static final String enroll = "ENROLL";

    private int codeBank;
    private int codeFilial;
    private String nameAES;
    private static int number;
    private int julianDate;

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
                firstChar, codeBank, codeFilial, point, line, enroll, point, julianDate);
    }
}
