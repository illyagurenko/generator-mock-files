package ru.itone.illya4gurenko.model;

public record Footer(
        int countRecords
) {
    private static final char footer  = 'T';
    private static final char filler = ' ';

    @Override
    public String toString() {
        return String.format("%1s%9s%10s",
                footer, filler, countRecords);
    }
}
