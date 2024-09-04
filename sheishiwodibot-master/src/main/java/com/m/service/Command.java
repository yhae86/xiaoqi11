package com.m.service;

import com.m.StringFactory;
import com.m.bot.impl.TelegramBotImpl;
import com.m.game.Game;
import com.m.game.GameList;
import com.m.game.Status;
import com.m.sql.entity.Telegram_group;
import com.m.sql.entity.Telegram_user;
import com.m.sql.service.TelegramGroupService;
import com.m.sql.service.TelegramUserService;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.validation.constraints.NotNull;

/**
 * 命令处理类，转入命令 仅命令名 （无‘/’，无‘@****’）
 */
public class Command {
    private final static String setLanguage ="setlanguage";
    private final static String newGame ="newgame";
    private final static String help ="help";
    private final static String exit ="exit";
    private final static String record ="record";
    private final static String start ="start";

    public static void process(@NotNull String command, Message message, boolean groupMessage, TelegramBotImpl telegramBot){
        if(!groupMessage&&(command.equals(newGame)||command.equals(exit))) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            Telegram_user user = TelegramUserService.getUser(message.getFrom().getId() + "");
            sendMessage.setText(StringFactory.getCommand_in_group(user.getLanguage()));
            telegramBot.sendMessage(sendMessage);
        }
        if(command.equals(start)&&!groupMessage){
            GameList.UnsentUserWord unsentUserWords = GameList.getUnsentUserWords(message.getFrom().getId());
            if(null!=unsentUserWords){
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChat().getId().toString());
                sendMessage.setText(unsentUserWords.messageText);
                telegramBot.sendMessage(sendMessage);
            }
        }else if(command.equals(record)){
            Telegram_user user = TelegramUserService.getUser(message.getFrom().getId().toString());
            SendMessage sendMessage = new SendMessage(message.getChatId().toString()
                    ,StringFactory.getRecord(user.getLanguage(),user)
                    , ParseMode.HTML,true,true,message.getMessageId(),null,null,true
                    );

            telegramBot.sendMessage(sendMessage,120000);

        }else if(command.equals(setLanguage)){
            if(groupMessage)
            telegramBot.deleteMessage(message);
            telegramBot.sendLanguageSet(message.getChatId().toString());
        }else if(command.equals(newGame)&&groupMessage){
            Game game = GameList.getGame(message.getChatId());
            Telegram_group group = TelegramGroupService.getGroup(message.getChatId().toString());
            if(null==game){
                SendMessage sendMessage= new SendMessage(message.getChatId().toString(),
                        StringFactory.getUserCreateGame(TelegramBotImpl.Name(message.getFrom()),
                                group.getLanguage()
                        )
                );
                telegramBot.sendMessage(sendMessage);
                GameList.createGame(group,message,telegramBot);
            }else{
                if(game.getStatus()!=Status.settlement) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(message.getChatId().toString());
                    sendMessage.setParseMode(ParseMode.HTML);
                    sendMessage.setText(TelegramBotImpl.Name(message.getFrom()) + "," +
                            StringFactory.getIn_the_game(group.getLanguage()));
                    if (game.getStatus() == Status.dengdaijiaru) {
                        sendMessage.setReplyMarkup(TelegramBotImpl.getJoinGameMarkup(group.getLanguage(), false));
                        telegramBot.sendMessage(sendMessage, 0, Status.dengdaijiaru, game);
                    } else {
                        telegramBot.sendMessage(sendMessage, 10000);
                    }
                }else {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(message.getChatId().toString());
                    sendMessage.setText(StringFactory.getGameSettlement(game.getLanguage()));
                    telegramBot.sendMessage(sendMessage, 0,Status.settlement,game);
                }
            }
        }else if(command.equals(help)){
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            String language;
            if(groupMessage) {
                Telegram_group group = TelegramGroupService.getGroup(message.getChatId().toString());
                language = group.getLanguage();
            }else {
                Telegram_user user = TelegramUserService.getUser(message.getFrom().getId().toString());
                language = user.getLanguage();
            }
            sendMessage.setText(StringFactory.getHelp(language));
            telegramBot.sendMessage(sendMessage,100000);
        }else if(command.equals(exit)){
            Game game = GameList.getGame(message.getChatId());
            Telegram_group group = TelegramGroupService.getGroup(message.getChatId().toString());
            if(null!=game&&null!=game.getMember(message.getFrom().getId())){
                if(game.getStatus()!=Status.settlement) {
                    game.setStatus(Status.close);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(message.getChatId().toString());
                    TelegramUserService.newExitGame(message.getFrom().getId().toString());
                    TelegramUserService.upFraction(message.getFrom().getId().toString(),-2);
                    sendMessage.setText(StringFactory.getExitGame(TelegramBotImpl.Name(message.getFrom()),
                            group.getLanguage()));
                    telegramBot.sendMessage(sendMessage);
                }else {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(message.getChatId().toString());
                    sendMessage.setText(StringFactory.getGameSettlement(game.getLanguage()));
                    telegramBot.sendMessage(sendMessage, 0,Status.settlement,game);
                }
            }
        }
    }
}
