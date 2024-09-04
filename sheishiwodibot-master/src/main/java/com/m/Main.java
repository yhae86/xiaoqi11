package com.m;

import com.m.bin.Bin;
import com.m.bot.impl.TelegramBotImpl;
import com.m.runtime.Task;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.meta.generics.TelegramBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main extends Thread {
    public static boolean botrun = true;
    public static Logger logger= LoggerFactory.getLogger(Main.class);


    public static TelegramBot bot;

    public static void main(String[]args){
        Task.init();
        TelegramBotsApi telegramBotsApi = null;
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            BotSession botSession = null;
            botSession = telegramBotsApi.registerBot(new TelegramBotImpl());
            StringFactory.main();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        while (botrun) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    @Test
    public void test(){
        logger.info( Bin.stringSub ("123456789",3));
        logger.info(  "123456789" );
    }
}