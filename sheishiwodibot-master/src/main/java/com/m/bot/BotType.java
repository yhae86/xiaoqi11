package com.m.bot;

public class BotType {
    public final static String TelegramBot = "TelegramBot";

    String typeName;

    public BotType(String typeName){
        this.typeName =typeName;
    }
    public String getTypeName() {
        return typeName;
    }

    public static BotType getType(String typeName){
       return new BotType(typeName);
    }
}
