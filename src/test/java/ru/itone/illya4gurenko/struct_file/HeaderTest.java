package ru.itone.illya4gurenko.struct_file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test generating header")
class HeaderTest {

    @Test
    @DisplayName("Test IMMEDIATE")
    void testHeaderImmediate() {
        Header header = new Header();
        String result = header.toString();

        assertAll("IMMEDIATE Header validation",
                () -> assertNotNull(result, "header mustn't be null"),
                () -> assertTrue(result.startsWith("H "), "header must start with 'H '"),
                () -> assertTrue(result.endsWith("IMMEDIATE"), "header must end with 'IMMEDIATE'")
        );
    }

    @Test
    @DisplayName("Test INTIME")
    void testHeaderIntime() {
        LocalDate targetDate = LocalDate.of(2026, 8, 10);
        LocalTime targetTime = LocalTime.of(15, 30, 0);
        Header header = new Header(targetDate, targetTime);

        String result = header.toString();

        assertAll("INTIME Header validation",
                () -> assertNotNull(result, "result mustn't be null"),
                () -> assertTrue(result.startsWith("H "), "result must start with 'H '"),
                () -> assertTrue(result.contains("INTIME   "), "result must contain 'INTIME'"),
                () -> assertTrue(result.endsWith("20260810 153000"), "result must end with target date and time")
        );
    }
}