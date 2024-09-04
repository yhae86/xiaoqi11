package com.m.bot.impl;

import com.m.bin.Bin;
import com.m.bot.Bot;
import com.m.bot.BotType;
import com.m.game.Game;
import com.m.game.Status;
import com.m.runtime.Task;
import com.m.service.ButtonEvent;
import com.m.service.Command;
import com.m.service.TextMessage;
import com.m.StringFactory;
import com.m.sql.service.TelegramGroupService;
import org.apache.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class TelegramBotImpl extends TelegramWebhookBot implements Bot,LongPollingBot  {
   static String botUsername ;
    static  String botUserId ;
    static String botToken;
    BotType botType ;
   public static TelegramBotImpl telegramBot;
    Logger logger = Logger.getLogger(TelegramBotImpl.class);
    @Override
    public void onUpdateReceived(Update update) {
        if (null!=update.getMessage()){
            boolean isGroupMessage =false;
            boolean isCommand = false;
            boolean atBotUsername =false;
            Message message = update.getMessage();
            //是群消息
            if(message.isGroupMessage()||message.isSuperGroupMessage()){
                isGroupMessage = true;
            }
            //  @ botUsername
            if(null!=message.getText()&&message.getText().endsWith("@"+botUsername)){
                atBotUsername=true;

            }
            //是命令
            if(message.isCommand()){
                isCommand =true;
                String command =
                        atBotUsername
                                ?
                                message.getText().substring(
                                1, message.getText().length()-botUsername.length()-1)
                                :
                                message.getText().substring(
                                1, message.getText().length())
                        ;
                Command.process(
                        command,
                        message,
                        isGroupMessage,this
               );

            }
            if(null!=message.getLeftChatMember()) {
                User leftChatMember = message.getLeftChatMember();
                if(botUsername.equals(leftChatMember.getUserName())){
                    TelegramGroupService.leftChatMember(message.getChatId().toString());
                }
            }
            if(null!=message.getNewChatMembers()) {
                List<User> newChatMembers = message.getNewChatMembers();
                for (User user:newChatMembers)
                if(botUsername.equals(user.getUserName())){
                    TelegramGroupService.newChatMember(message.getChatId().toString());
                    sendLanguageSet(message.getChatId().toString());
                }
            }
            if(!atBotUsername&&!isCommand) TextMessage.newMessage(message,isGroupMessage,this);
        }
        if(null!=update.getCallbackQuery()) {
            boolean isGroupMessage =false;
            //是群消息
            if(update.getCallbackQuery().getMessage().isGroupMessage()||update.getCallbackQuery().getMessage().isSuperGroupMessage()){
                isGroupMessage = true;
            }
            ButtonEvent.onClick(update,isGroupMessage);
        }
    }
    public void editMessage(Message message, String newMessageText, InlineKeyboardMarkup replyMarkup){
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(message.getChatId().toString());
        editMessageText.setMessageId(message.getMessageId());
        editMessageText.setText(newMessageText);
        editMessageText.setReplyMarkup(replyMarkup);
        editMessageText.setParseMode(ParseMode.HTML);
        try {
            this.executeAsync(editMessageText);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public TelegramBotImpl() throws TelegramApiException {
        botType = BotType.getType(BotType.TelegramBot);
        Properties properties = new Properties();
        InputStream inputStream=null;
        try {
             inputStream= new FileInputStream(Bin.getProjectPath()+"/telegram.properties");
       } catch (FileNotFoundException | UnsupportedEncodingException e) {
            inputStream= getClass().getClassLoader().getResourceAsStream("telegram.properties");
        }
        if(null==inputStream)return;
        try {
            properties.load(inputStream);
            botUsername = properties.getProperty("username");
            botToken = properties.getProperty("token");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        telegramBot=this;
            botUserId=getMe().getId().toString();
    }
    @Override
    public String getBotUsername() {
        return botUsername;
    }
    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void clearWebhook() throws TelegramApiRequestException {
    }

    public Message sendMessage(SendMessage message){
        try {
            message.setParseMode(ParseMode.HTML);
            return this.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void sendMessage(SendMessage message, long autoDeleteTime){
        try {
            message.setParseMode(ParseMode.HTML);
            Message execute = this.execute(message);
            Task.addAutoDeleteMessage(execute,autoDeleteTime,this);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public Message sendMessage(SendMessage message, long autoDeleteTime, Status status,Game game){
        try {
            message.setParseMode(ParseMode.HTML);
            Message execute = this.execute(message);
            Task.addAutoDeleteMessage(execute,autoDeleteTime,this,status,game);
            return execute;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void deleteMessage(Message message){
        try {
            this.executeAsync(new DeleteMessage(message.getChatId()+"",message.getMessageId()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return null;
    }

    @Override
    public String getBotPath() {
        return Bin.getRealPath();
    }

    public void sendLanguageSet(@NonNull String chatId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(StringFactory.getPleaseSetLanguage());
        sendMessage.setReplyMarkup(StringFactory.getSetLanguageButtons());
        try {
            executeAsync(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    @Override
    public BotType getBotType() {
        return botType;
    }


    public static String NameText(User user) {
        String  name ="";
        name+=user.getFirstName()+" ";
        if(user.getLastName()!=null)name+=user.getLastName();
        if(name.length()>20)name= name.substring(0,20);
        return name;

    }
    public static String Name(User user) {
        String  name ="";
        name+=user.getFirstName()+" ";
        if(user.getLastName()!=null)name+=user.getLastName();
        if(name.length()>20)name= name.substring(0,20);
        return "<a href=\"tg://user?id="+user.getId()+"\" >"+name+"</a>";

    }
    public static InlineKeyboardMarkup getJoinGameMarkup(String language,boolean startButton){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton joinGame =new InlineKeyboardButton();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", ButtonEvent.join_game);
        joinGame.setCallbackData(jsonObject.toString());
        joinGame.setText(com.m.StringFactory.getJoinGame(language));
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(joinGame);
        rows.add(rowInline1);
        if(startButton){
            InlineKeyboardButton startGame =new InlineKeyboardButton();
            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("action", ButtonEvent.ready);
            startGame.setCallbackData(jsonObject2.toString());
            startGame.setText(com.m.StringFactory.getReady(language));
            List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
            rowInline2.add(startGame);
            rows.add(rowInline2);
        }
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
    public static InlineKeyboardMarkup getViewWord(String language){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton viewWord =new InlineKeyboardButton();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", ButtonEvent.viewWord);
        viewWord.setCallbackData(jsonObject.toString());
        viewWord.setText(com.m.StringFactory.getViewWord(language));
        viewWord.setUrl("t.me/"+botUsername);
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(viewWord);
        rows.add(rowInline1);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
}