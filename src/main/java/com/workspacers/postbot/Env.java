package com.workspacers.postbot;

import java.util.*;
import java.util.stream.Collectors;

public final class Env {
    public final String botToken;
    public final String botUsername;
    public final long groupChatId;
    public final String dbPath;
    public final String postImagePath;
    public final Set<Long> allowedUserIds; // empty => allow all (NOT recommended)

    private Env(String botToken, String botUsername, long groupChatId, String dbPath, String postImagePath, Set<Long> allowedUserIds) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.groupChatId = groupChatId;
        this.dbPath = dbPath;
        this.postImagePath = postImagePath;
        this.allowedUserIds = allowedUserIds;
    }

    public static Env load() {
        String token = getenvRequired("BOT_TOKEN");
        String username = getenv("BOT_USERNAME", "PostPublisherBot");
        long groupId = parseLong(getenv("GROUP_CHAT_ID", "-1003060928185"), -1003060928185L);
        String db = getenv("DB_PATH", "/data/bot.db");
        String img = getenv("POST_IMAGE_PATH", "/app/1.jpg");

        String allowed = getenv("ALLOWED_USER_IDS", "").trim();
        Set<Long> allowedIds = new HashSet<>();
        if (!allowed.isEmpty()) {
            allowedIds = Arrays.stream(allowed.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> parseLong(s, -1))
                    .filter(v -> v > 0)
                    .collect(Collectors.toSet());
        }

        return new Env(token, username, groupId, db, img, allowedIds);
    }

    private static String getenvRequired(String key) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Missing required env var: " + key);
        }
        return v;
    }

    private static String getenv(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    private static long parseLong(String s, long def) {
        try { return Long.parseLong(s); } catch (Exception e) { return def; }
    }
}
