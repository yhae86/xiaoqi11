package com.m.sql.service;


import com.m.Main;
import com.m.bin.Bin;
import com.m.game.Game;
import com.m.sql.MybatisUtil;
import com.m.sql.entity.Telegram_group;
import com.m.sql.mapper.Telegram_groupMapper;
import org.apache.ibatis.session.SqlSession;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.util.List;

public class TelegramGroupService extends Thread{
    static SqlSession sqlSession = null;
    static Telegram_groupMapper telegram_groupMapper =null;
    static TelegramGroupService telegramGroupService;
    static boolean sqlSessionIsException = false;
    public static List<Telegram_group> selectAll(){
        SqlSession sqlSession = null;
       return telegram_groupMapper.selectAll();
    }
    public static int addGroup(Telegram_group telegram_group){
        int rows=0;
        try {
            rows += telegram_groupMapper.insertSelective(telegram_group);
            if(rows>0) sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSessionIsException=true;
            if (null!= sqlSession) sqlSession.rollback();
            rows=0;
        }
        return rows;
    }

    public static  void upFinisgGame(Game game) {
        String s = game.chatId.toString();
        int rows=0;
        try {
            rows += telegram_groupMapper.upFinisgGame(s);
            if(rows>0) sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSessionIsException=true;
            if (null!= sqlSession) sqlSession.rollback();
        }
    }
    public static  void upMaxOfPeople(Game game,Integer people){
        String s = game.chatId.toString();
        int rows=0;
        try {
            rows += telegram_groupMapper.upMaxOfPeople(s,people);
            if(rows>0) sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSessionIsException=true;
            if (null!= sqlSession) sqlSession.rollback();
        }
    }

    public static void updateGroupData(Chat chat){
        int rows=0;
        Telegram_group group = getGroup(chat.getId().toString());
        if(null!=group){
            if(null==group.getUserName())group.setUserName("");
            if(null==group.getTitle())group.setTitle("");
            if(!group.getUserName().equals(chat.getUserName())||
                    !group.getTitle().equals(chat.getTitle())
            ){
                group.setUserName(Bin.stringSub(chat.getUserName(),24));
                group.setTitle(Bin.stringSub(chat.getTitle(),24));
                try {
                    rows += telegram_groupMapper.updateGroupData(group);
                    if(rows>0) sqlSession.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    sqlSessionIsException=true;
                    if (null!= sqlSession) sqlSession.rollback();
                    rows=0;
                }
            }
        }
    }
    public static void updateLanguage(String groupId, String language){

        int rows=0;
        try {
            Telegram_group group = getGroup(groupId);
            rows+= telegram_groupMapper.updateLanguage(groupId,language);
            if(rows>0) sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSessionIsException=true;
            if (null!= sqlSession) sqlSession.rollback();
            rows=0;
        }
    }
    public static Telegram_group getGroup(String groupId){
        Telegram_group telegram_group =null;
        try {
            telegram_group= telegram_groupMapper.selectByGroupId(groupId);
            if (null == telegram_group) {
                telegram_group = new Telegram_group();
                telegram_group.setGroupId(groupId);
                TelegramGroupService.addGroup(telegram_group);
                telegram_group = telegram_groupMapper.selectByGroupId(groupId);
            }
        }catch (Exception e){e.printStackTrace();sqlSessionIsException=true;}
        return telegram_group;
    }
    static{
        try {
            sqlSession = MybatisUtil.createSqlSession();
            telegram_groupMapper = sqlSession.getMapper(Telegram_groupMapper.class);
        } catch (Exception e) {
            e.printStackTrace();
            sqlSessionIsException = true;
        }
        telegramGroupService= new TelegramGroupService();
        telegramGroupService.start();
    }

    public static void newChatMember(String toString) {
        getGroup(toString);
        telegram_groupMapper.newChatMember(toString);
    }

    public static void leftChatMember(String toString) {
        telegram_groupMapper.leftChatMember(toString);
    }

    @Override
    public void run(){
        while (true){
            if(sqlSessionIsException){
                Main.logger.error("TelegramGroupService 会话正在重启。。");
                MybatisUtil.closeSqlSession(sqlSession);
                try {
                    sqlSession = MybatisUtil.createSqlSession();
                    telegram_groupMapper = sqlSession.getMapper(Telegram_groupMapper.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    sqlSessionIsException = true;
                }
               sqlSessionIsException=false;
            }
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static Telegram_groupMapper getMapperNotAutoSubmit() {
        return telegram_groupMapper;
    }

}
