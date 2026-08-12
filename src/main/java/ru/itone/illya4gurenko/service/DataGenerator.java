package ru.itone.illya4gurenko.service;


/**
 * Стратегия генератора данных клиента.
 */
public interface DataGenerator {
    /**
     * Генерирует одну строку клиентской записи фиксированной ширины.
     *
     * @return Форматированная строка с ФИО, номером карты, типом операции и суммой
     */
    String generateData();
}
