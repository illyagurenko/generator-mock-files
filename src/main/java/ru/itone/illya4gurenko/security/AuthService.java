package ru.itone.illya4gurenko.security;

import ru.itone.illya4gurenko.config.Base;

import java.util.Map;

public class AuthService extends Base {
    private static final AuthService INSTANCE = new AuthService();
    private static final Map<String, String> USER_WHITELIST = Map.of(
            "q1", "12345678",
            "q2", "212121",
            "q3", "654hgfd3"
    );

    private AuthService() {}

    public static AuthService getInstance() {
        return INSTANCE;
    }

    public boolean authorize(String username, String encryptedPassword) {
        if (username == null || encryptedPassword == null) {
            return false;
        }

        String trueUser = USER_WHITELIST.get(username);
        if (trueUser == null) {
            return false;
        }

        try {
            String decryptedPassword = getCryptoService().decrypt(encryptedPassword);
            return trueUser.equals(decryptedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}