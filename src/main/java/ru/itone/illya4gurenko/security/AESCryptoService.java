package ru.itone.illya4gurenko.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AESCryptoService {

    private static final Logger log= LoggerFactory.getLogger(AESCryptoService.class);

    private static final String ALGORITHM = "AES";

    private static final AESCryptoService INSTANCE = new AESCryptoService();

    private SecretKey secretKey;

    private boolean isDisableCrypto = false;

    public static AESCryptoService getInstance() {

        return INSTANCE;
    }
    private AESCryptoService() {
    }

    public void init(String keyProperty) {
        this.secretKey = initSecretKey(keyProperty);
    }

    private SecretKey initSecretKey(String keyProperty) {
        try {
            if (keyProperty == null || keyProperty.isBlank() || keyProperty.isEmpty()) {
                log.info("Key not enter");
                return null;
            }
            Path keyFilePath = Paths.
                    get(keyProperty);
            String base64Key = new String(Files.readAllBytes(keyFilePath));

            byte[] decodedKey = Base64.getDecoder().decode(base64Key.trim()); // Ожидается Base64 строка в файле
            isDisableCrypto = false;
            return new SecretKeySpec(decodedKey, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize encryption key from file", e);
        }
    }

    public String decrypt(String encryptedText) {
        if (isDisableCrypto) {
            log.info("password not decrypt");
            return encryptedText;
        }
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] decode = decoder.decode(encryptedText.getBytes());
            byte[] decryptedBytes;
            decryptedBytes = decrypt(decode, secretKey);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException |
                 NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String encrypt(String decryptedText) {
        if (isDisableCrypto) {
            return decryptedText;
        }

        try {
            Base64.Encoder encoder = Base64.getEncoder();

            byte[] encryptedBytes;
            encryptedBytes = encrypt(decryptedText.getBytes(), secretKey);
            byte[] encode = encoder.encode(encryptedBytes);
            return new String(encode, StandardCharsets.UTF_8);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException |
                 NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] decrypt(byte[] encryptedBytes, SecretKey secretKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.
                getInstance(ALGORITHM);
        cipher.init(Cipher.
                DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedBytes);
    }

    private byte[] encrypt(byte[] decryptedBytes, SecretKey secretKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.
                getInstance(ALGORITHM);
        cipher.init(Cipher.
                ENCRYPT_MODE, secretKey);
        return cipher.doFinal(decryptedBytes);
    }
}
