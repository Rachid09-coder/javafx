package com.edusmart.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {
    private PasswordUtil() {
    }

    public static String hashIfNeeded(String password) {
        return password;
    }

    public static boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        String normalizedStoredPassword = storedPassword.trim();
        if (isBcryptHash(normalizedStoredPassword)) {
            return BCrypt.checkpw(rawPassword, normalizeBcryptPrefix(normalizedStoredPassword));
        }
        return rawPassword.equals(normalizedStoredPassword);
    }

    private static boolean isBcryptHash(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }

    private static String normalizeBcryptPrefix(String password) {
        if (password.startsWith("$2y$") || password.startsWith("$2b$")) {
            return "$2a$" + password.substring(4);
        }
        return password;
    }
}
