package ru.itone.illya4gurenko.struct_file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test generating title file")
class TitleFileTest {

    @DisplayName("Test generating title file")
    @Test
    void testGenerateTitle(){
        TitleFile file = new TitleFile(5, 12, "AES");
        String result = file.toString();

        assertTrue(result.startsWith("Z005012.AES_ENROLL"), "title must equals pattern");
        assertTrue(result.startsWith("Z"), "title must start with char: Z");
    }

    @DisplayName("Test generating title file")
    @Test
    void testIncrementFiles(){
        TitleFile file1 = new TitleFile(1, 2, "TEST");
        TitleFile file2 = new TitleFile(1, 2, "TEST");

        int number1 = Integer.parseInt(file1.toString().split("ENROLL")[1].split("\\.")[0]);
        int number2 = Integer.parseInt(file2.toString().split("ENROLL")[1].split("\\.")[0]);

        assertEquals(number1 + 1, number2, "counter must increment 1");


    }

}