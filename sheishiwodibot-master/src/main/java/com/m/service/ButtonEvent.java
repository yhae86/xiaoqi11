package com.m.service;

import com.m.StringFactory;
import com.m.bin.MyJSONObject;
import com.m.bot.impl.TelegramBotImpl;
import com.m.game.Game;
import com.m.game.GameList;
import com.m.game.Status;
import com.m.sql.service.TelegramGroupService;
import com.m.sql.service.TelegramUserService;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ButtonEvent {
    public final static String join_game = "join game";
    public final static String setLanguage = "setLanguage";
    public final static String vote ="vote";
    public final static String ready ="ready";
    public final static String viewWord ="ViewWord";

    static  Logger logger = Logger.getLogger(ButtonEvent.class);
    public static void onClick(Update update,boolean isGroupMessage){
        CallbackQuery callbackQuery = update.getCallbackQuery();
        MyJSONObject jsonObject = new MyJSONObject(callbackQuery.getData());
        String action = jsonObject.getString("action");
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());

        switch (action) {
            case setLanguage:
                if (isGroupMessage) {
                    TelegramGroupService.updateLanguage(callbackQuery.getMessage().getChatId().toString(),
                            jsonObject.getString("language"));
                    TelegramUserService.updateLanguage(callbackQuery.getFrom().getId().toString()
                            , jsonObject.getString("language"));
                    TelegramBotImpl.telegramBot.editMessage(
                            callbackQuery.getMessage(),
                            StringFactory.getLanguageSetFinish(TelegramBotImpl.Name(callbackQuery.getFrom())
                                    , jsonObject.getString("language"))
                            , null
                    );

                    Game game = GameList.getGame(callbackQuery.getMessage().getChatId());
                    if (game != null) game.setLanguage(jsonObject.getString("language"));
                } else {
                    TelegramUserService.updateLanguage(callbackQuery.getFrom().getId().toString()
                            , jsonObject.getString("language"));
                    TelegramBotImpl.telegramBot.editMessage(
                            callbackQuery.getMessage(),
                            "OK !"
                            , null
                    );
                }
                break;
            case join_game: {
                Game game = GameList.getGame(callbackQuery.getMessage().getChatId());
                if (null != game && game.getStatus() == Status.dengdaijiaru) {
                    game.joinGame(callbackQuery.getFrom());
                }
                break;
            }
            case ready: {
                Game game = GameList.getGame(callbackQuery.getMessage().getChatId());
                if (null != game && game.getStatus() == Status.dengdaijiaru) {
                    game.memberReady(callbackQuery.getFrom().getId());
                }
                break;
            }
            case vote: {
                Game game = GameList.getGame(callbackQuery.getMessage().getChatId());
                if (null != game && game.getStatus() == Status.toupiaozhong) {
                    if(callbackQuery.getFrom().getId().equals(jsonObject.getLong("to"))){
                        answerCallbackQuery.setText(StringFactory.getNotVoteSelf(game.getLanguage()));
                    }else {
                        if(game.vote(callbackQuery.getFrom(),jsonObject.getLong("to")))
                        answerCallbackQuery.setText(StringFactory.getSuccess(game.getLanguage()));
                    }
                }
                break;
            }
        }
        try {
            TelegramBotImpl.telegramBot.executeAsync(answerCallbackQuery);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
