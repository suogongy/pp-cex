package com.ppcex.user.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class UserNoGenerator {

    private final AtomicLong sequence = new AtomicLong(0);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 生成用户编号
     * 格式：U + 时间戳(14位) + 序列号(4位)
     * 例如：U202409171200000001
     */
    public synchronized String generateUserNo() {
        String timestamp = LocalDateTime.now().format(formatter);
        long seq = sequence.incrementAndGet() % 10000;
        return String.format("U%s%04d", timestamp, seq);
    }

    /**
     * 生成邀请码
     * 格式：8位随机字母数字组合
     */
    public String generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        return code.toString();
    }

    /**
     * 生成会话ID
     * 格式：时间戳 + 随机字符串
     */
    public String generateSessionId() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return timestamp + random;
    }
}