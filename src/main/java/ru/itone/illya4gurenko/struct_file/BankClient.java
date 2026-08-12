package ru.itone.illya4gurenko.struct_file;

/**
 * Класс структуры записи клиента генерируемого файла.
 * Создается с фиксированной шириной
 *
 * @param fullName ФИО клиента до 100 символов
 * @param account  Номер счета до 30 символов
 * @param type     Тип операции: {@link Type} CR, ZR, DR 2 символа
 * @param amount   Сумма транзакции до 20 символов
 */
public record BankClient(
        String fullName,
        String account,
        Type type,
        String amount
) {
    /**
     * Преобразует запись клиента в строку фиксированной ширины согласно шаблону.
     * @return Форматированная строка инофрмации о клиенте
     */
    @Override
    public String toString() {
        return String.format("%-100s%-30s%2s%20s",
                fullName, account, type, amount);
    }
}