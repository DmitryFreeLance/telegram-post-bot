package com.workspacers.postbot;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class PostContent {
    private PostContent() {
    }

    public static final String HTML_TEXT = """
            Приветствую Вас на канале о недвижимости Виктора Пешехонова — основателя Агентства недвижимости «Белый город»
            
            Предложим Вам самые ликвидные и достойные варианты недвижимости для жизни и бизнеса 💼✨:
             🏠 Квартиры в новостройках Москвы и на вторичном рынке
             🚀 Закрытые старты продаж
             🔥 Акционные предложения и рассрочки
             🏢 Объекты с арендаторами и для собственного бизнеса
             🌍 Большой выбор недвижимости в г. Нижний Новгород, Сочи, Санкт-Петербург или ОАЭ
             
            Продаём объекты наших клиентов максимально дорого, быстро и красиво 💎⚡📸
            
            Подбирают или продают недвижимость близкие Вам люди? 🤝
            Рекомендуйте и получайте гарантированный % 💰✅
            """;

    public static final String CTA_URL = "https://t.me/BestRealtor77_bot";
    public static final String CTA_TEXT = "\uD83D\uDD25 Связаться";

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
