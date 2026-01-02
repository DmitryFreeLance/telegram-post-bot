package com.workspacers.postbot;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class PostContent {
    private PostContent() {}

    public static final String HTML_TEXT = """
А ВЫ ЗНАЕТЕ, СКОЛЬКО ПЛАТИТЕ ЗА СВОИ МЫСЛИ? 💵💰

Что, если я вам скажу, что ваша усталость, отсутствие энергии, раздражительность или проблемы в отношениях — это не ваша вина?
А всего лишь «счёт» за ваши мысли, убеждения и чувства, по которому вы исправно платите каждый месяц?

За что? 💸

💰 За убеждение о том, что деньги достаются тяжелым трудом;
💰 За мысль, что доверять — опасно, а быть собой — стыдно;
💰 За веру, что нужно всё контролировать, чтобы не случилось чего плохого;
💰 За ощущение, что вы живёте «не свою жизнь», но ничего не меняете.
""";

    public static final String CTA_URL = "https://t.me/BezzPanikiBot?start=2";
    public static final String CTA_TEXT = "\uD83D\uDD25 Пройти ЧЕК-АП";

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
