package ru.itone.illya4gurenko.struct_file;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Header {
    private static final char HEADER = 'H';

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

    private static final char FILLER = ' ';

    private final ProcType procType;
    private final LocalDate currentDate;
    private final LocalTime currentTime;
    private final LocalDate inTimeDate;
    private final LocalTime inTimeTime;

    public Header(LocalDate inTimeDate, LocalTime inTimeTime) {
        this.procType = ProcType.INTIME;
        this.currentDate = LocalDate.now();
        this.currentTime = LocalTime.now();
        this.inTimeDate = inTimeDate;
        this.inTimeTime = inTimeTime;
    }

    public Header() {
        this.procType = ProcType.IMMEDIATE;
        this.currentDate = LocalDate.now();
        this.currentTime = LocalTime.now();
        this.inTimeDate = null;
        this.inTimeTime = null;
    }

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
