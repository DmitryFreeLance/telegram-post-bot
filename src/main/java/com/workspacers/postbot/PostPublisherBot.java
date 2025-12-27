package com.workspacers.postbot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class PostPublisherBot extends TelegramLongPollingBot {
    private static final String CB_PREVIEW = "PREVIEW_POST";
    private static final String CB_PUBLISH = "PUBLISH_POST";
    private static final String CB_FORCE_PUBLISH = "FORCE_PUBLISH_POST";

    private final Env env;
    private final Db db;

    public PostPublisherBot(Env env) {
        super(env.botToken);
        this.env = env;
        this.db = new Db(env.dbPath);
    }

    @Override
    public String getBotUsername() {
        return env.botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) handleMessage(update.getMessage());
            else if (update.hasCallbackQuery()) handleCallback(update.getCallbackQuery());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(Message msg) throws TelegramApiException {
        if (!msg.hasText()) return;

        String text = msg.getText().trim();
        long chatId = msg.getChatId();
        long userId = (msg.getFrom() != null) ? msg.getFrom().getId() : -1;

        if (text.equals("/start") || text.equals("/panel")) {
            SendMessage sm = new SendMessage();
            sm.setChatId(chatId);
            sm.setText("""
Привет! Я публикую подготовленный пост в вашу группу по кнопке.

Нажми «Предпросмотр», чтобы увидеть пост у себя в чате.
Нажми «Опубликовать пост», и я отправлю фото + текст в группу.
""");
            sm.setReplyMarkup(publishKeyboard(false));
            execute(sm);
            return;
        }

        if (text.equals("/preview")) {
            if (!isAllowed(userId)) {
                execute(simple(chatId, "⛔️ У вас нет доступа к предпросмотру."));
                return;
            }
            previewToUser(chatId);
            return;
        }

        if (text.equals("/publish")) {
            if (!isAllowed(userId)) {
                execute(simple(chatId, "⛔️ У вас нет доступа к публикации."));
                return;
            }
            publishToGroup(userId, false, chatId);
            return;
        }

        if (text.equals("/force_publish")) {
            if (!isAllowed(userId)) {
                execute(simple(chatId, "⛔️ У вас нет доступа к публикации."));
                return;
            }
            publishToGroup(userId, true, chatId);
        }
    }

    private void handleCallback(CallbackQuery cq) throws TelegramApiException {
        String data = cq.getData();
        long userId = (cq.getFrom() != null) ? cq.getFrom().getId() : -1;
        long chatId = cq.getMessage().getChatId();

        if (CB_PREVIEW.equals(data)) {
            if (!isAllowed(userId)) {
                execute(answer(cq.getId(), "Нет доступа"));
                execute(simple(chatId, "⛔️ У вас нет доступа к предпросмотру."));
                return;
            }
            execute(answer(cq.getId(), "Отправляю предпросмотр..."));
            previewToUser(chatId);
            return;
        }

        if (CB_PUBLISH.equals(data) || CB_FORCE_PUBLISH.equals(data)) {
            if (!isAllowed(userId)) {
                execute(answer(cq.getId(), "Нет доступа"));
                execute(simple(chatId, "⛔️ У вас нет доступа к публикации."));
                return;
            }

            boolean force = CB_FORCE_PUBLISH.equals(data);
            execute(answer(cq.getId(), force ? "Публикую (форс)..." : "Публикую..."));
            publishToGroup(userId, force, chatId);
        }
    }

    /**
     * Отправляет пост пользователю в текущий чат (предпросмотр).
     */
    private void previewToUser(long chatId) throws TelegramApiException {
        File photo = new File(env.postImagePath);
        if (!photo.exists() || !photo.isFile()) {
            execute(simple(chatId, "❌ Не найден файл изображения: " + env.postImagePath + "\n" +
                    "Укажите корректный путь в POST_IMAGE_PATH и перезапустите контейнер."));
            return;
        }

        SendPhoto sp = new SendPhoto();
        sp.setChatId(chatId);
        sp.setPhoto(new InputFile(photo));
        sp.setCaption(PostContent.HTML_TEXT);
        sp.setParseMode("HTML");
        sp.setReplyMarkup(ctaKeyboard());

        execute(sp);
    }

    private void publishToGroup(long requestedBy, boolean force, long notifyChatId) throws TelegramApiException {
        String hash = PostContent.contentHash();
        Optional<Long> lastMsg = db.findLastPostedMessageIdByHash(hash);

        if (!force && lastMsg.isPresent()) {
            SendMessage sm = simple(notifyChatId,
                    "✅ Этот пост уже публиковался раньше (message_id=" + lastMsg.get() + ").\n" +
                            "Если нужно повторить — нажмите «Опубликовать повторно» или используйте /force_publish.");
            sm.setReplyMarkup(publishKeyboard(true));
            execute(sm);
            return;
        }

        File photo = new File(env.postImagePath);
        if (!photo.exists() || !photo.isFile()) {
            execute(simple(notifyChatId, "❌ Не найден файл изображения: " + env.postImagePath + "\n" +
                    "Укажите корректный путь в POST_IMAGE_PATH и перезапустите контейнер."));
            db.insertPost(requestedBy, hash, null);
            return;
        }

        SendPhoto sp = new SendPhoto();
        sp.setChatId(env.groupChatId);
        sp.setPhoto(new InputFile(photo));
        sp.setCaption(PostContent.HTML_TEXT);
        sp.setParseMode("HTML");
        sp.setReplyMarkup(ctaKeyboard());

        Message sent = execute(sp);

        db.insertPost(requestedBy, hash, sent.getMessageId() != null ? sent.getMessageId().longValue() : null);
        execute(simple(notifyChatId, "✅ Пост опубликован в группе (chat_id=" + env.groupChatId +
                ", message_id=" + sent.getMessageId() + ")."));
    }

    private InlineKeyboardMarkup publishKeyboard(boolean showForce) {
        InlineKeyboardButton preview = new InlineKeyboardButton("Предпросмотр");
        preview.setCallbackData(CB_PREVIEW);

        InlineKeyboardButton publish = new InlineKeyboardButton("Опубликовать пост");
        publish.setCallbackData(CB_PUBLISH);

        if (!showForce) {
            return new InlineKeyboardMarkup(List.of(
                    List.of(preview),
                    List.of(publish)
            ));
        }

        InlineKeyboardButton republish = new InlineKeyboardButton("Опубликовать повторно");
        republish.setCallbackData(CB_FORCE_PUBLISH);

        return new InlineKeyboardMarkup(List.of(
                List.of(preview),
                List.of(publish),
                List.of(republish)
        ));
    }

    private InlineKeyboardMarkup ctaKeyboard() {
        InlineKeyboardButton cta = new InlineKeyboardButton(PostContent.CTA_TEXT);
        cta.setUrl(PostContent.CTA_URL);
        return new InlineKeyboardMarkup(List.of(List.of(cta)));
    }

    private boolean isAllowed(long userId) {
        // If ALLOWED_USER_IDS is not set - allow everyone who can talk to bot (simple mode).
        // Recommended: set ALLOWED_USER_IDS to your Telegram user id(s).
        if (env.allowedUserIds == null || env.allowedUserIds.isEmpty()) return true;
        return env.allowedUserIds.contains(userId);
    }

    private SendMessage simple(long chatId, String text) {
        SendMessage sm = new SendMessage();
        sm.setChatId(chatId);
        sm.setText(text);
        return sm;
    }

    private AnswerCallbackQuery answer(String id, String text) {
        AnswerCallbackQuery a = new AnswerCallbackQuery();
        a.setCallbackQueryId(id);
        a.setText(text);
        a.setShowAlert(false);
        return a;
    }
}