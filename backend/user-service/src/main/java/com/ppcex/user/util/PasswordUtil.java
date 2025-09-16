package com.ppcex.user.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PasswordUtil {

    private final BCryptPasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    public PasswordUtil() {
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.secureRandom = new SecureRandom();
    }

    /**
     * 生成密码盐值
     */
    public String generateSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 加密密码
     */
    public String encodePassword(String rawPassword, String salt) {
        String passwordWithSalt = rawPassword + salt;
        return passwordEncoder.encode(passwordWithSalt);
    }

    /**
     * 验证密码
     */
    public boolean matches(String rawPassword, String encodedPassword, String salt) {
        String passwordWithSalt = rawPassword + salt;
        return passwordEncoder.matches(passwordWithSalt, encodedPassword);
    }

    /**
     * 生成随机密码
     */
    public String generateRandomPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("密码长度不能小于8位");
        }

        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";

        String allChars = uppercase + lowercase + digits + specialChars;
        StringBuilder password = new StringBuilder();

        // 确保包含至少一个大写字母、一个小写字母、一个数字和一个特殊字符
        password.append(uppercase.charAt(secureRandom.nextInt(uppercase.length())));
        password.append(lowercase.charAt(secureRandom.nextInt(lowercase.length())));
        password.append(digits.charAt(secureRandom.nextInt(digits.length())));
        password.append(specialChars.charAt(secureRandom.nextInt(specialChars.length())));

        // 填充剩余长度
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(secureRandom.nextInt(allChars.length())));
        }

        // 打乱字符顺序
        return shuffleString(password.toString());
    }

    /**
     * 打乱字符串顺序
     */
    private String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }

    /**
     * 检查密码强度
     */
    public boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (isSpecialChar(c)) {
                hasSpecialChar = true;
            }
        }

        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }

    /**
     * 判断是否为特殊字符
     */
    private boolean isSpecialChar(char c) {
        String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        return specialChars.indexOf(c) != -1;
    }
}