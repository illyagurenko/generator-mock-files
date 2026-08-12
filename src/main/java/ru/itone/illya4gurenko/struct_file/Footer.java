package ru.itone.illya4gurenko.struct_file;

/**
 * Класс структуры последней строки генерируемого файла.
 * Создается с фиксированной шириной
 *
 * @param countRecords количество записей о клиенте в файле
 */
public record Footer(
        int countRecords
) {
    /** Маркер последней строки */
    private static final char FOOTER = 'T';

    /** Разделитель полей */
    private static final char FILLER = ' ';

    /**
     * Преобразует последнюю строку файла в строку фиксированной ширины согласно шаблону.
     *
     * @return Форматированная последняя строка
     */
    @Override
    public String toString() {
        return String.format("%1s%9s%10s",
                FOOTER, FILLER, countRecords);
    }
}
