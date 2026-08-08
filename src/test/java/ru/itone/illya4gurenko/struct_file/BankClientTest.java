package ru.itone.illya4gurenko.struct_file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test generating record file")
class BankClientTest {

    @DisplayName("Test generating record")
    @Test
    void testRecord() {
        BankClient bankClient = new BankClient(
                "Зайцев Даниил Денисович",
                "4804976586086739",
                Type.ZR,
                "164092"
        );

        String result = bankClient.toString();
        String validRecord = "Зайцев Даниил Денисович                                                                             4804976586086739              ZR              164092";

        assertEquals(validRecord, result, "record must equals pattern");

    }
}