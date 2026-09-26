package ru.itone.illya4gurenko.service;


import ru.itone.illya4gurenko.struct_file.GruVistaTab;

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
    GruVistaTab generateGruVistaRecord();
}
