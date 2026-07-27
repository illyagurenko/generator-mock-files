package ru.itone.illya4gurenko.model;

public record Footer(
        int countRecords
) {
    private static final char FOOTER = 'T';
    private static final char FILLER = ' ';

    @Override
    public String toString() {
        return String.format("%1s%9s%10s",
                FOOTER, FILLER, countRecords);
    }
}
