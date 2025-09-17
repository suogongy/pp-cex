package com.ppcex.user.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    private PasswordUtil passwordUtil;

    @BeforeEach
    void setUp() {
        passwordUtil = new PasswordUtil();
    }

    @Test
    void generateSalt_ShouldGenerateDifferentSalts() {
        String salt1 = passwordUtil.generateSalt();
        String salt2 = passwordUtil.generateSalt();

        assertNotNull(salt1);
        assertNotNull(salt2);
        assertNotEquals(salt1, salt2);
        assertEquals(24, salt1.length()); // Base64 encoded 16 bytes
    }

    @Test
    void encodePassword_ShouldHashPasswordWithSalt() {
        String password = "MyPassword123!";
        String salt = passwordUtil.generateSalt();

        String hashedPassword = passwordUtil.encodePassword(password, salt);

        assertNotNull(hashedPassword);
        assertNotEquals(password, hashedPassword);
        assertTrue(hashedPassword.length() > 0);
    }

    @Test
    void matches_ShouldValidateCorrectPassword() {
        String password = "MyPassword123!";
        String salt = passwordUtil.generateSalt();
        String hashedPassword = passwordUtil.encodePassword(password, salt);

        boolean result = passwordUtil.matches(password, hashedPassword, salt);

        assertTrue(result);
    }

    @Test
    void matches_ShouldRejectIncorrectPassword() {
        String password = "MyPassword123!";
        String wrongPassword = "WrongPassword123!";
        String salt = passwordUtil.generateSalt();
        String hashedPassword = passwordUtil.encodePassword(password, salt);

        boolean result = passwordUtil.matches(wrongPassword, hashedPassword, salt);

        assertFalse(result);
    }

    @Test
    void isStrongPassword_ValidPassword_ShouldReturnTrue() {
        String validPassword = "StrongPass123!";

        boolean result = passwordUtil.isStrongPassword(validPassword);

        assertTrue(result);
    }

    @Test
    void isStrongPassword_TooShort_ShouldReturnFalse() {
        String shortPassword = "Short1!";

        boolean result = passwordUtil.isStrongPassword(shortPassword);

        assertFalse(result);
    }

    @Test
    void isStrongPassword_NoUppercase_ShouldReturnFalse() {
        String password = "lowercase123!";

        boolean result = passwordUtil.isStrongPassword(password);

        assertFalse(result);
    }

    @Test
    void isStrongPassword_NoLowercase_ShouldReturnFalse() {
        String password = "UPPERCASE123!";

        boolean result = passwordUtil.isStrongPassword(password);

        assertFalse(result);
    }

    @Test
    void isStrongPassword_NoDigit_ShouldReturnFalse() {
        String password = "NoDigitPassword!";

        boolean result = passwordUtil.isStrongPassword(password);

        assertFalse(result);
    }

    @Test
    void isStrongPassword_NoSpecialChar_ShouldReturnFalse() {
        String password = "NoSpecialChar123";

        boolean result = passwordUtil.isStrongPassword(password);

        assertFalse(result);
    }

    @Test
    void isStrongPassword_NullPassword_ShouldReturnFalse() {
        boolean result = passwordUtil.isStrongPassword(null);

        assertFalse(result);
    }

    @Test
    void generateRandomPassword_ValidLength_ShouldGeneratePassword() {
        int length = 12;
        String password = passwordUtil.generateRandomPassword(length);

        assertNotNull(password);
        assertEquals(length, password.length());
        assertTrue(passwordUtil.isStrongPassword(password));
    }

    @Test
    void generateRandomPassword_TooShort_ShouldThrowException() {
        int length = 7;

        assertThrows(IllegalArgumentException.class, () -> {
            passwordUtil.generateRandomPassword(length);
        });
    }

    @Test
    void generateRandomPassword_EightLength_ShouldBeStrong() {
        int length = 8;
        String password = passwordUtil.generateRandomPassword(length);

        assertTrue(passwordUtil.isStrongPassword(password));
        assertEquals(length, password.length());
    }
}