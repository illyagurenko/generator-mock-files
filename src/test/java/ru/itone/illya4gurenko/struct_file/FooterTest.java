package ru.itone.illya4gurenko.struct_file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test generating footer")
class FooterTest {
    private Footer footer;
    private String result;

    @BeforeEach
    void initFooter(){
        footer = new Footer(12);
        result = footer.toString();
    }

    @DisplayName("Test footer on null")
    @Test
    void testFooterNotNull(){
        assertNotNull(result, "result mustn`t be null");
    }

    @DisplayName("Test footer start with T")
    @Test
    void testFooterStartWithT(){
        assertTrue(result.startsWith("T "), "result must start with char: T");
    }

    @DisplayName("Test footer equals pattern")
    @Test
    void testFooterEqualsPattern(){
        String validResult = "T                 12";
        assertEquals(validResult, result, "footer must equals pattern");
    }



}