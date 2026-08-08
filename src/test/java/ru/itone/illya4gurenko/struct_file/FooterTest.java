package ru.itone.illya4gurenko.struct_file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test generating footer")
class FooterTest {

    @DisplayName("Test generating footer")
    @Test
    void testGenerateFooter(){
        Footer footer = new Footer(12);

        String result = footer.toString();
        String validResult = "T                 12";

        assertNotNull(result, "result mustn`t be null");
        assertEquals(validResult, result, "footer must equals pattern");
        assertTrue(result.startsWith("T "), "result must start with char: T");
    }



}