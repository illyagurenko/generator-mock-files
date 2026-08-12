package ru.itone.illya4gurenko.security;

import ru.itone.illya4gurenko.config.Base;

import java.util.Map;

/**
 * Сервис авторизации клиентов приложения.
 * <p>
 * Сверяет имя пользователя со списком разрешенных учетных записей,
 * а также дешифрует переданный зашифрованный пароль через {@link AESCryptoService}
 * и проверяет его совпадение.
 * </p>
 */
public class AuthService extends Base {

    private static final AuthService INSTANCE = new AuthService();
    private final AESCryptoService cryptoService;
    private static final Map<String, String> USER_WHITELIST = Map.of(
            "q1", "12345678",
            "q2", "212121",
            "q3", "654hgfd3"
    );

    private AuthService() {
        this.cryptoService = getCryptoService();
    }

    public static AuthService getInstance() {
        return INSTANCE;
    }

    AuthService(AESCryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    /**
     * Выполняет аутентификацию и авторизацию пользователя.
     *
     * @param username          Имя пользователя
     * @param encryptedPassword Зашифрованный в Base64 пароль из HTTP-заголовка
     * @return {@code true}, если пользователь найден в белом списке и пароль совпал; {@code false} в противном случае
     */
    public boolean authorize(String username, String encryptedPassword) {
        debug("authenticating user: {}", username);
        if (username == null || encryptedPassword == null) {
            warn("authentication failed, missing username or password header");
            return false;
        }

        String trueUser = USER_WHITELIST.get(username);
        if (trueUser == null) {
            warn("authentication failed: user '{}' not found in whitelist", username);
            return false;
        }

        try {
            String decryptedPassword = this.cryptoService.decrypt(encryptedPassword);
            debug("user '{}' authenticated successfully", username);
            return trueUser.equals(decryptedPassword);
        } catch (Exception e) {
            error("authentication error during decryption for user: {}", username, e);
            return false;
        }
    }
}