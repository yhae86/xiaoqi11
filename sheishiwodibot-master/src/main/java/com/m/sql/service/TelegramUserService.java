package com.m.sql.service;

import com.m.Main;
import com.m.bin.Bin;
import com.m.sql.MybatisUtil;
import com.m.sql.entity.Telegram_user;
import com.m.sql.mapper.Telegram_userMapper;
import org.apache.ibatis.session.SqlSession;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

public  class TelegramUserService extends Thread{
    static SqlSession sqlSession = null;
    static Telegram_userMapper telegram_userMapper =null;
    static TelegramUserService telegramUserService;
    static boolean sqlSessionIsException = false;
    public static List<Telegram_user> selectAll(){
        List<Telegram_user> list= null;;
        try {
            list = telegram_userMapper.selectAll();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSessionIsException = true;
        }
        return list;
    }
    public static boolean addUser(Telegram_user telegram_user){
        int rows=0;
        try {
            rows += telegram_userMapper.insertSelective(telegram_user);
            if(rows>0) sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSessionIsException=true;
            if (null!= sqlSession) sqlSession.rollback();
            rows=0;
        }
        return rows>0;
    }


    public static void upFraction(String telegram_id,Integer fraction ) {
        int rows=0;
        try {
            rows += telegram_userMapper.upFraction(telegram_id,fraction);
            if(rows>0) sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSessionIsException=true;
            if (null!= sqlSession) sqlSession.rollback();
        }
    }
    public static  void newCompleteGame(String[] telegram_id) {
        if(telegram_id==null||telegram_id.length==0)return;

        int rows=0;
        try {
            rows += telegram_userMapper.newCompleteGame(telegram_id);
            if(rows>0) sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSessionIsException=true;
            if (null!= sqlSession) sqlSession.rollback();
        }
    }
    public static  void newExitGame(String telegram_id){
        int rows=0;
        try {
            rows += telegram_userMapper.newExitGame(telegram_id);
            if(rows>0) sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSessionIsException=true;
            if (null!= sqlSession) sqlSession.rollback();
        }
    }
    public static  void newWord_people(String[] telegram_id){
        if(telegram_id==null||telegram_id.length==0)return;
        int rows=0;
        try {
            rows += telegram_userMapper.newWord_people(telegram_id);
            if(rows>0) sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSessionIsException=true;
            if (null!= sqlSession) sqlSession.rollback();
        }
    }
    public static  void newWord_spy(String[] telegram_id){
        if(telegram_id==null||telegram_id.length==0)return;
        int rows=0;
        try {
            rows += telegram_userMapper.newWord_spy(telegram_id);
            if(rows>0) sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSessionIsException=true;
            if (null!= sqlSession) sqlSession.rollback();
            rows=0;
        }
    }
    public static  void newWord_people_victory(String[] telegram_id){
        if(telegram_id==null||telegram_id.length==0)return;
        int rows=0;
        try {
            rows += telegram_userMapper.newWord_people_victory(telegram_id);
            if(rows>0) sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSessionIsException=true;
            if (null!= sqlSession) sqlSession.rollback();
            rows=0;
        }
    }
    public static  void newWord_spy_victory(String[] telegram_id){
        if(telegram_id==null||telegram_id.length==0)return;
        int rows=0;
        try {
            rows += telegram_userMapper.newWord_spy_victory(telegram_id);
            if(rows>0) sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSessionIsException=true;
            if (null!= sqlSession) sqlSession.rollback();
            rows=0;
        }
    }

    public static Telegram_user getUser(String userId){
        Telegram_user telegram_user =null;
        try {
            telegram_user= telegram_userMapper.selectByUserId(userId);
            if (null == telegram_user) {
                telegram_user = new Telegram_user();
                telegram_user.setTelegram_id(userId);
                TelegramUserService.addUser(telegram_user);
                telegram_user = telegram_userMapper.selectByUserId(userId);
            }
        }catch (Exception e){e.printStackTrace();sqlSessionIsException=true;}
        return telegram_user;
    }
    public static void upJoinGame(User user){
        int rows=0;
        try {
            rows += telegram_userMapper.newJoinGame(user.getId().toString());
            if(rows>0) sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSessionIsException=true;
            if (null!= sqlSession) sqlSession.rollback();
            rows=0;
        }
    }
    public static void updateUserData(User user){
        int rows=0;
        Telegram_user dbUser = getUser(user.getId().toString());
        if(null!=dbUser){
            if(null==dbUser.getUserName())dbUser.setUserName("");
            if(null==dbUser.getFirstName())dbUser.setFirstName("");
            if(null==dbUser.getLastName())dbUser.setLastName("");
           if(!dbUser.getUserName().equals(user.getUserName())||
               !dbUser.getFirstName().equals(user.getFirstName())||
               !dbUser.getLastName().equals(user.getLastName())
           ){
               dbUser.setUserName(Bin.stringSub(user.getUserName(),24));
               dbUser.setFirstName(Bin.stringSub(user.getFirstName(),24));
               dbUser.setLastName(Bin.stringSub(user.getLastName(),24));
               try {
                   rows += telegram_userMapper.updateUserData(dbUser);
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

    public static void updateLanguage(String telegram_id, String language){

        int rows=0;
        try {
            Telegram_user user = getUser(telegram_id);
            rows+= telegram_userMapper.updateLanguage(telegram_id,language);
            if(rows>0) sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
            sqlSessionIsException=true;
            if (null!= sqlSession) sqlSession.rollback();
            rows=0;
        }
    }
    static{
        try {
            sqlSession = MybatisUtil.createSqlSession();
            telegram_userMapper = sqlSession.getMapper(Telegram_userMapper.class);
        } catch (Exception e) {
            e.printStackTrace();
            sqlSessionIsException = true;
        }
        telegramUserService= new TelegramUserService();
        telegramUserService.start();
    }
    @Override
    public void run(){
        while (true){
            if(sqlSessionIsException){
                Main.logger.error("TelegramUserService 会话正在重启。。");
                MybatisUtil.closeSqlSession(sqlSession);
                try {
                    sqlSession = MybatisUtil.createSqlSession();
                    telegram_userMapper = sqlSession.getMapper(Telegram_userMapper.class);
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

}
