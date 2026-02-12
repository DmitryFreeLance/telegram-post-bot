package com.workspacers.postbot;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class PostContent {
    private PostContent() {
    }

    public static final String HTML_TEXT = """
            Удобный бот для оформления заказа
            """;

    public static final String CTA_URL = "https://t.me/RollsRoms_bot";
    public static final String CTA_TEXT = "\uD83D\uDD25 Сделать заказ";

    public static String contentHash() {
        // stable hash used to avoid duplicate posting
        return sha256(HTML_TEXT + "|" + CTA_URL + "|" + CTA_TEXT);
    }

    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
