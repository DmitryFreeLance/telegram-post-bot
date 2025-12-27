package com.workspacers.postbot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) throws Exception {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        PostPublisherBot bot = new PostPublisherBot(Env.load());
        botsApi.registerBot(bot);
        System.out.println("Bot started: @" + bot.getBotUsername());
    }
}
