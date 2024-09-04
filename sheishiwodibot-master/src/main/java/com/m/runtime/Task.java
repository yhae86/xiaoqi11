package com.m.runtime;

import com.m.Main;
import com.m.bot.impl.TelegramBotImpl;
import com.m.game.Game;
import com.m.game.GameList;
import com.m.game.Status;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.LinkedList;

public class Task extends Thread{
    public static Task task;
    public static LinkedList<GroupAdmin> groupAdmins = new LinkedList<>();
    public static final LinkedList<AutoDeleteMessage> autoDeleteMessages = new LinkedList<>();
    public static long currentTime ;//当前时间
    public static void init(){
        task =new Task();
        task.start();
    }


    @Override
    public void run(){
        TimeInterception groupAdminsTime=new TimeInterception(5000);
        TimeInterception autoDeleteMessageTime=new TimeInterception(1000);

        while (Main.botrun){//   1秒执行一次 ，1s Run
            try {

                currentTime = System.currentTimeMillis();

                synchronized (GameList.unsentUserWords){
                    GameList.unsentUserWords.removeIf(unsentUserWord -> !unsentUserWord.game.run);
                }

                for (int i = 0;i<autoDeleteMessages.size();i++) {
                    AutoDeleteMessage next = autoDeleteMessages.get(i);
                    if(currentTime-next.createTime>next.deleteTime||
                            (next.game!=null&&next.game.getStatus()!=next.status)){
                        try {
                            next.telegramBot.execute(new DeleteMessage(next.message.getChatId().toString(),
                                    next.message.getMessageId()));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        synchronized (autoDeleteMessages) {
                            autoDeleteMessages.remove(next);
                        }
                    }
                }

            }catch (Exception e) {e.printStackTrace();}

            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    boolean passing(TimeInterception timeInterception){
        if(currentTime - timeInterception.LastTime>timeInterception.cycle) {
            timeInterception.LastTime=currentTime;
            return true;
        }
        return false;
    }
    static class TimeInterception{
        long cycle;
        long LastTime;

        public void setLastTime(long lastTime) {
            LastTime = lastTime;
        }
        public TimeInterception(long cycle) {
            this.cycle = cycle;
        }

    }
    public static void addAutoDeleteMessage(Message message,long deleteTime,TelegramBotImpl telegramBot){
        AutoDeleteMessage autoDeleteMessage = new AutoDeleteMessage();
        autoDeleteMessage.createTime = currentTime;
        autoDeleteMessage.message = message;
        autoDeleteMessage.deleteTime = deleteTime;
        autoDeleteMessage.telegramBot = telegramBot;
        synchronized (autoDeleteMessages) {
            autoDeleteMessages.add(autoDeleteMessage);
        }

    }
    public static void addAutoDeleteMessage(Message message
            ,long deleteTime
            ,TelegramBotImpl telegramBot
            ,Status status
            , Game game){
        AutoDeleteMessage autoDeleteMessage = new AutoDeleteMessage();
        autoDeleteMessage.createTime = currentTime;
        autoDeleteMessage.message = message;
        autoDeleteMessage.deleteTime = deleteTime>0?deleteTime:1200000;
        autoDeleteMessage.telegramBot = telegramBot;
        autoDeleteMessage.status = status;
        autoDeleteMessage.game = game;
        synchronized (autoDeleteMessages) {
            autoDeleteMessages.add(autoDeleteMessage);
        }

    }

    static class GroupAdmin{
        String groupName;
        boolean isAdmin;
        long endTime;
    }
    static class AutoDeleteMessage{
        Message message;
        long deleteTime;
        long createTime;
        Status status=null;
        Game game=null;
        TelegramBotImpl telegramBot;
    }
}