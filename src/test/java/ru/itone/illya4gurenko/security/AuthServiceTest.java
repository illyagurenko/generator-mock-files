package ru.itone.illya4gurenko.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test service autorization")
class AuthServiceTest {

    @Mock
    private AESCryptoService cryptoServiceMock;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(cryptoServiceMock);
    }

    @Test
    @DisplayName("success authorize user from whiteList")
    void testAuthorizeSuccess() {
        String username = "q1";
        String encryptedPass = "e12345678";
        String decryptedPass = "12345678";

        when(cryptoServiceMock.decrypt(encryptedPass)).thenReturn(decryptedPass);

        boolean isAuthorized = authService.authorize(username, encryptedPass);

        assertTrue(isAuthorized, "q1 from whiteList success authorize");
    }

    @Test
    @DisplayName("unsuccess authorize user from whiteList")
    void testAuthorizeWrongPassword() {

        String username = "q1";
        String encryptedPass = "e12345";
        String decryptedPass = "12345";

        when(cryptoServiceMock.decrypt(encryptedPass)).thenReturn(decryptedPass);

        boolean isAuthorized = authService.authorize(username, encryptedPass);

        assertFalse(isAuthorized, "incorrect password");
    }

    @Test
    @DisplayName("unsuccess authorize user not from whiteList")
    void testAuthorizeUnknownUser() {

        boolean isAuthorized = authService.authorize("qqq", "123");

        assertFalse(isAuthorized, "unknown user");
        verifyNoInteractions(cryptoServiceMock);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Invalid data")
    void testAuthorizeEmptyInputs(String emptyInput) {
        assertAll(
                () -> assertFalse(authService.authorize(emptyInput, "pass"), "empty user"),
                () -> assertFalse(authService.authorize("q1", emptyInput), "empty user"),
                () -> assertFalse(authService.authorize(null, "pass"), "null user")
        );



    }


}