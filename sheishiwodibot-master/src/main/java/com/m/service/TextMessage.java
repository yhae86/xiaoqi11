package com.m.service;

import com.m.bot.impl.TelegramBotImpl;
import com.m.game.Game;
import com.m.game.GameList;
import org.telegram.telegrambots.meta.api.objects.Message;

public class TextMessage {

    public static void newMessage(Message message, boolean groupMessage, TelegramBotImpl telegramBot){
        String text = message.getText();
        if(text!=null){
            if(groupMessage){
                Game game = GameList.getGame(message.getChatId());
                if(game!=null)game.speak(message.getFrom().getId());
            }else {
            }
        }
    }
}
