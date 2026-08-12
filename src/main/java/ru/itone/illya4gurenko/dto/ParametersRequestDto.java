package ru.itone.illya4gurenko.dto;

import java.time.LocalDateTime;

/**
 * DTO-модель входящего HTTP-запроса на генерацию файла
 *
 * @param codeBank     код банка
 * @param codeFilial   код филиала
 * @param nameAES      Имя автоматизированной системы
 * @param countRecords Количество генерируемых клиентских записей в одном файле
 * @param countFiles   Количество генерируемых файлов
 * @param inTime       Плановое время обработки файла

 */
public record ParametersRequestDto(
        int codeBank,
        int codeFilial,
        String nameAES,
        int countRecords,
        int countFiles,
        LocalDateTime inTime
) {
}
