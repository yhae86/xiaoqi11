package com.m.game;

import com.m.bot.impl.TelegramBotImpl;
import com.m.sql.entity.Telegram_group;
import com.m.sql.service.TelegramGroupService;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;

public class GameList {
    static final List<Game> gameList = new ArrayList<>();
    public static final List<UnsentUserWord> unsentUserWords = new ArrayList<>();

    public synchronized static void createGame(Telegram_group group, Message message, TelegramBotImpl telegramBot) {
        Game game = new Game(group, message, telegramBot);
        synchronized (gameList) {
            gameList.add(game);
        }
        TelegramGroupService.updateGroupData(message.getChat());
    }

    public synchronized static void removeGame(Game game) {
        if (null == game) return;
        game.run = false;
        synchronized (gameList) {
            gameList.remove(game);
        }
    }

    public static Game getGame(Long chatId) {
        try {
            for (int i = 0; i < gameList.size(); i++) {
                Game game = gameList.get(i);
                if (game != null && chatId.equals(game.chatId)) return game;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public synchronized static void addUnsentUserWords(UnsentUserWord unsentUserWord) {
        synchronized (unsentUserWords) {
            unsentUserWords.add(unsentUserWord);
        }
    }

    public static UnsentUserWord getUnsentUserWords(long userId) {
        synchronized (unsentUserWords) {
            try {
                for (UnsentUserWord userWord : unsentUserWords) {
                    if (userWord.userId == userId) {
                        unsentUserWords.remove(userWord);
                        return userWord;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static class UnsentUserWord {
        public String messageText;
        public long createTime;
        long userId;
        public Game game;
        public UnsentUserWord(String messageText, long createTime, long userId,Game game) {
            this.messageText = messageText;
            this.createTime = createTime;
            this.userId = userId;
            this.game = game;
        }

    }
}