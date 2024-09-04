package com.m.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface Bot {
    Message sendMessage(SendMessage message);
    public BotType getBotType();
}
