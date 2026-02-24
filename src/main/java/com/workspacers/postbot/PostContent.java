package com.workspacers.postbot;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class PostContent {
    private PostContent() {
    }

    public static final String HTML_TEXT = """
            Matrx AI запущен 🚀
            
            Создавай текст, фото и видео за пару сообщений.
            Выбирай модель под задачу — и получай результат сразу в Telegram.
            
            Что уже внутри:
            🧠 Текст — Gemini 3
            📸 Фото — Flux 2, Ideogram V3, NanoBanana
            🎬 Видео — Veo 3.1, Sora 2, Kling 3.0
            
            Попробуй сейчас — это быстро и удобно.
            """;

    public static final String CTA_URL = "https://t.me/MatrxAIBot";
    public static final String CTA_TEXT = "\uD83D\uDD25 Запустить";

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
