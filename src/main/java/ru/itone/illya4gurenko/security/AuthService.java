package ru.itone.illya4gurenko.security;

import java.util.Map;

public class AuthService {

    private static final Map<String, String> USER_WHITELIST = Map.of(
            "q1", "12345678",
            "q2", "212121",
            "q3", "654hgfd3"
    );

    private final AESCryptoService cryptoService;

    public AuthService(AESCryptoService cryptoService) {
        this.cryptoService = cryptoService;
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
            String decryptedPassword = cryptoService.decrypt(encryptedPassword);
            return trueUser.equals(decryptedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}