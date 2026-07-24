package ru.itone.illya4gurenko.model;

public record BankClient(
        String fullName,
        String account,
        Type type,
        String amount
) {
    @Override
    public String toString() {
        return String.format("%-100s%-30s%2s%20s",
                fullName, account, type, amount);
    }
}
