package ru.itone.illya4gurenko.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Teat AES crypto coder")
class AESCryptoServiceTest {

    @Test
    @DisplayName("test encrypt->decrypt")
    void testEncryptDecryptCycle() {
        AESCryptoService cryptoService = AESCryptoService.getInstance();
        String originalPass = "12345678";

        String encrypted = cryptoService.encrypt(originalPass);
        String decrypted = cryptoService.decrypt(encrypted);

        assertAll("crypto cycle",
                () -> assertNotNull(encrypted),
                () -> assertEquals(originalPass, decrypted, "decrypt pass must equals original")
        );
    }

    @Test
    @DisplayName("Invalid Base64")
    void testDecryptInvalidBase64() {
        AESCryptoService cryptoService = AESCryptoService.getInstance();

        assertThrows(RuntimeException.class, () -> cryptoService.decrypt("INVALID_NOT_BASE64!@#$"));
    }

}