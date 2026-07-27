package ru.itone.illya4gurenko.dto;

import java.time.LocalDateTime;

public record ParametersRequestDto(
        int codeBank,
        int codeFilial,
        String nameAES,
        int countRecords,
        int countFiles,
        LocalDateTime inTime
) {
}
