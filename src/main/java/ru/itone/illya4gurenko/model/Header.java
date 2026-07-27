package ru.itone.illya4gurenko.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Header {
    private final ProcType procType;
    private final LocalDate sendDate;
    private final LocalTime sendTime;
    private static final char HEADER = 'H';
    private static final char FILLER = ' ';

    public Header(LocalDate sendDate, LocalTime sendTime) {
        this.procType = ProcType.INTIME;
        this.sendDate = sendDate;
        this.sendTime = sendTime;
    }

    public Header() {
        this.procType = ProcType.IMMEDIATE;
        this.sendDate = LocalDate.now();
        this.sendTime = LocalTime.now();
    }

    @Override
    public String toString() {
        return String.format("%1s%1s%8s%1s%6s%1s%-9s",
                HEADER,
                FILLER,
                sendDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                FILLER,
                sendTime.format(DateTimeFormatter.ofPattern("HHmmss")),
                FILLER,
                procType
        );
    }
}
