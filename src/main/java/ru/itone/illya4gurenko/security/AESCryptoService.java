package ru.itone.illya4gurenko.security;
import ru.itone.illya4gurenko.config.Base;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
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

/**
 * Сервис симметричного шифрования и дешифрования данных по алгоритму <b>AES</b>.
 * <p>
 * Секретный ключ инициализируется из файла, путь к которому задается в свойстве {@code crypto.key.path}.
 * Если файл с ключом не найден или не задан, шифрование переходит в безопасный выключенный режим
 * ({@code isDisableCrypto = true}), и строки возвращаются в исходном виде.
 * </p>
 */
public class AESCryptoService extends Base {

    private static final String ALGORITHM = "AES";
    private static final AESCryptoService INSTANCE = new AESCryptoService();

    private final SecretKey secretKey;
    private boolean isDisableCrypto = true;

    public static AESCryptoService getInstance() {
        return INSTANCE;
    }

    private AESCryptoService() {
        this.secretKey = initSecretKey();
    }

    /**
     * Инициализирует секретный ключ AES, считывая его из Base64-файла на диске.
     *
     * @return Объект {@link SecretKey} или {@code null}, если файл ключа недоступен
     */
    private SecretKey initSecretKey() {
        try {
            String keyProperty = config.getCryptoKeyPath();

            if (keyProperty == null || keyProperty.isBlank()) {
                info("Crypto Key path is not set. Encryption is disabled.");
                this.isDisableCrypto = true;
                return null;
            }

            Path keyFilePath = Paths.get(keyProperty);
            if (!Files.exists(keyFilePath)) {
                warn("Crypto key file not found at: {}. Encryption disabled.", keyProperty);
                this.isDisableCrypto = true;
                return null;
            }

            String base64Key = new String(Files.readAllBytes(keyFilePath), StandardCharsets.UTF_8);
            byte[] decodedKey = Base64.getDecoder().decode(base64Key.trim());

            this.isDisableCrypto = false;
            info("AES Crypto Service initialized successfully.");
            return new SecretKeySpec(decodedKey, ALGORITHM);

        } catch (Exception e) {
            error("Failed to initialize encryption key from file", e);
            this.isDisableCrypto = true;
            return null;
        }
    }

    /**
     * Расшифровывает строку, зашифрованную по алгоритму AES и закодированную в Base64.
     *
     * @param encryptedText Зашифрованная строка в формате Base64
     * @return Исходная расшифрованная строка в UTF-8
     * @throws RuntimeException В случае ошибки криптографических операций
     */
    public String decrypt(String encryptedText) {
        if (isDisableCrypto || secretKey == null || encryptedText == null) {
            info("Password decryption skipped (crypto disabled or empty input)");
            return encryptedText;
        }

        try {
            byte[] decode = Base64.getDecoder().decode(encryptedText.getBytes(StandardCharsets.UTF_8));
            byte[] decryptedBytes = decrypt(decode, secretKey);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error during decryption", e);
        }
    }

    /**
     * Зашифровывает исходную строку алгоритмом AES и кодирует результат в Base64.
     *
     * @param decryptedText Исходный текст для шифрования
     * @return Зашифрованная строка в формате Base64
     * @throws RuntimeException В случае ошибки криптографических операций
     */
    public String encrypt(String decryptedText) {
        if (isDisableCrypto || secretKey == null || decryptedText == null) {
            return decryptedText;
        }

        try {
            byte[] encryptedBytes = encrypt(decryptedText.getBytes(StandardCharsets.UTF_8), secretKey);
            byte[] encode = Base64.getEncoder().encode(encryptedBytes);
            return new String(encode, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error during encryption", e);
        }
    }

    private byte[] decrypt(byte[] encryptedBytes, SecretKey secretKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedBytes);
    }

    private byte[] encrypt(byte[] decryptedBytes, SecretKey secretKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(decryptedBytes);
    }
}