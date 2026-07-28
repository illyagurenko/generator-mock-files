package ru.itone.illya4gurenko.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Base64;

public class AESCryptoService {

    private static final Logger log = LoggerFactory.getLogger(AESCryptoService.class);
    private static final String ALGORITHM = "AES";

    private final SecretKey secretKey;
    private boolean isDisableCrypto = false;

    public AESCryptoService(String keyProperty) {
        this.secretKey = initSecretKey(keyProperty);
    }

    private SecretKey initSecretKey(String keyProperty) {
        try {
            if (keyProperty == null || keyProperty.isBlank()) {
                log.info("Key path not provided. Cryptography is disabled.");
                this.isDisableCrypto = true;
                return null;
            }

            Path keyFilePath = Paths.get(keyProperty);
            if (!Files.exists(keyFilePath)) {
                log.warn("Key file not found at: {}. Cryptography is disabled.", keyProperty);
                this.isDisableCrypto = true;
                return null;
            }

            String base64Key = new String(Files.readAllBytes(keyFilePath), StandardCharsets.UTF_8);
            byte[] decodedKey = Base64.getDecoder().decode(base64Key.trim());
            this.isDisableCrypto = false;
            return new SecretKeySpec(decodedKey, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize encryption key from file", e);
        }
    }

    public String decrypt(String encryptedText) {
        if (isDisableCrypto || secretKey == null) {
            log.info("Decryption skipped: crypto is disabled.");
            return encryptedText;
        }
        try {
            byte[] decode = Base64.getDecoder().decode(encryptedText.getBytes(StandardCharsets.UTF_8));
            byte[] decryptedBytes = decrypt(decode, secretKey);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    public String encrypt(String decryptedText) {
        if (isDisableCrypto || secretKey == null) {
            return decryptedText;
        }
        try {
            byte[] encryptedBytes = encrypt(decryptedText.getBytes(StandardCharsets.UTF_8), secretKey);
            byte[] encode = Base64.getEncoder().encode(encryptedBytes);
            return new String(encode, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    private byte[] decrypt(byte[] encryptedBytes, SecretKey secretKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedBytes);
    }

    private byte[] encrypt(byte[] decryptedBytes, SecretKey secretKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(decryptedBytes);
    }
}