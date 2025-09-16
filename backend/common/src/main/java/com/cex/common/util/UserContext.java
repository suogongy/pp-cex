package com.cex.common.util;

public class UserContext {

    private static final ThreadLocal<Long> userId = new ThreadLocal<>();

    public static void setCurrentUserId(Long id) {
        userId.set(id);
    }

    public static Long getCurrentUserId() {
        return userId.get();
    }

    public static void clear() {
        userId.remove();
    }
}