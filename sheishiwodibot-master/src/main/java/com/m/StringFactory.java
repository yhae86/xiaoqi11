package com.m;

import com.m.service.ButtonEvent;
import com.m.sheishiwodi.word.Word;
import com.m.sql.entity.Telegram_user;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.net.URL;
import java.util.*;

public class StringFactory
{
    static Logger logger = Logger.getLogger(StringFactory.class);
    final static Map<String, List<Word>> wordMap =new HashMap<>();
    final static Map<String, Map<String,String>> stringsMap =new HashMap<>();
    final static Map<String, String> languageAliasMap =new HashMap<>();

    public static String getRecord(String language, Telegram_user telegram_user) {
        String record = getString(language,"record");
        return record.replace("{joinGame}", telegram_user.getJoinGame() + "")
                .replace("{completeGame}",telegram_user.getCompleteGame()+"")
                .replace("{exitGame}",telegram_user.getExitGame()+"")
                .replace("{word_people}",telegram_user.getWord_people()+"")
                .replace("{word_spy}",telegram_user.getWord_spy()+"")
                .replace("{word_people_victory}",telegram_user.getWord_people_victory()+"")
                .replace("{word_spy_victory}",telegram_user.getWord_spy_victory()+"")
                .replace("{fraction}",telegram_user.getFraction()+"");
    }
    public static String getGameSettlement(String language) {
        return getString(language,"gameSettlement");
    }
    public static String getAboutToVoteL(String language) {
        return getString(language,"aboutToVoteL");
    }
    public static String getAboutToVoteR(String language) {
        return getString(language,"aboutToVoteR");
    }

    public static String getNotAdmin(String language) {
        return getString(language,"notAdmin");
    }
    public static String getUserWordIs(String language,String user,String word) {
        return getString(language,"userWordIs").replace("{user}",user).
                replace("{word}",word);
    }

    public static String getEliminatedInThisRound(String language) {
        return getString(language,"eliminatedInThisRound");
    }
    public static String getRemainingPersonnel(String language,int remainingPeople,int totalPeople) {
        return getString(language,"remainingPersonnel").replace("{remainingPeople}",""+remainingPeople).
                replace("{totalPeople}",""+totalPeople);
    }

    public static String getGameOver(String language) {
        return getString(language,"gameOver");
    }
    public static String getEveryoneVoted(String language) {
        return getString(language,"everyoneVoted");
    }
    public static String getVotedTimeEnd(String language) {
        return getString(language,"votedTimeEnd");
    }
    public static String getAbstained(String userName, String language) {
        return getString(language,"abstained").replace("{user}", userName);
    }
    public static String getNotVote(String userName, String language) {
        return getString(language,"notVote").replace("{user}", userName);
    }
    public static String getSuccess(String language) {
        return getString(language,"success");
    }
    public static String getFailure(String language) {
        return getString(language,"failure");
    }
    public static String getAbstain(String language) {
        return getString(language,"abstain");
    }
    public static String getNotVoteSelf(String language) {
        return getString(language,"notVoteSelf");
    }
    public static String getVotingStart(String language) {
        return getString(language,"votingStart");
    }
    /**
     *
     * @param time 秒
     */
    public static String geSpeechTime(long time,int round, String language) {
        return getString(language,"speechTime").replace("{time}", time+"").replace("{round}",round+"");
    }
    public static String getExitGame(String userName, String language) {
        return getString(language,"exitGame").replace("{user}", userName);
    }
    public static String getGameStart(String language) {
        return getString(language,"gameStart");
    }
    public static String getViewWord(String language) {
        return getString(language,"ViewWord");
    }
    public static String getTimeoutShutdown(String language) {
        return getString(language,"TimeoutShutdown");
    }
    public static String getReady(String language) {
        return getString(language,"ready");
    }

    public static String getGamePlayerWaiting(String language) {
        return getString(language,"GamePlayerWaiting");
    }

    public static String getJoinGame(String language) {
        return getString(language,"joinGame");
    }

    public static String getIn_the_game(String language) {
        return getString(language,"InTheGame");
    }

    public static String getHelp(String language) {
        return getString(language,"help");
    }

    public static String getUserCreateGame(String userName, String language) {
        return getString(language,"userCreateGame").replace("{user}", userName);
    }
    public static String getLanguageSetFinish(String userName, String language) {
        return getString(language,"languageSetFinish").replace("{user}", userName)
                .replace("{alias}", languageAliasMap.get(language));
    }

    public static String getSendWord(String groupName, String word,String language) {
        return getString(language,"sendWord").replace("{groupName}", groupName).replace("{word}",word);
    }
    public static String getPleaseSetLanguage() {

        StringBuilder stringBuilder = new StringBuilder();

        for (String next : languageAliasMap.keySet()) {
            stringBuilder.append(getString(next,"PleaseSetLanguage")).append("\n");
        }
        return stringBuilder.toString();
    }

    public static String getCommand_in_group(String language) {
        return getString(language,"commandInGroup");
    }


    public static InlineKeyboardMarkup getSetLanguageButtons() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        int i =0;
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = null;
        for (String language : languageAliasMap.keySet()) {
            if (i % 2 == 0) {
                rowInline = new ArrayList<>();
                rows.add(rowInline);
            }
            i++;
            InlineKeyboardButton button = new InlineKeyboardButton();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", ButtonEvent.setLanguage);
            jsonObject.put("language", language);
            button.setCallbackData(jsonObject.toString());
            button.setText(languageAliasMap.get(language));
            rowInline.add(button);
        }
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }


   public static void main(){}
    static {
        init();
    }
    static String getString(String language,String stringName){
        return stringsMap.get(language).get(stringName);
    }
    public static Word getWord(String language){
        return wordMap.get(language).get((int) ((System.currentTimeMillis()/1000) % wordMap.get(language).size()));
    }
   static void  init(){
        Element rootElement ;
        try {
            logger.debug("StringFactory配置。。");
            wordMap.clear();
            stringsMap.clear();
            languageAliasMap.clear();
            URL resource = StringFactory.class.getResource("/strings.xml");
            if (resource == null) logger.error("resource 为 null");
            else logger.debug("开始加载 " + resource.getPath());
            SAXReader reader = new SAXReader();
            Document document = null;
            try {
                document = reader.read(resource);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            assert document != null;
            rootElement = document.getRootElement();
            Iterator iterator = rootElement.elementIterator();
            while (iterator.hasNext()) {
                Element languageElement = (Element) iterator.next();//语言
                languageAliasMap.put(languageElement.attributeValue("language"), languageElement.attributeValue("alias"));
                Element strings = languageElement.element("strings");
                if (null != strings) {
                    Map<String, String> _stringsMap = new HashMap<>();
                    StringFactory.stringsMap.put(languageElement.attributeValue("language"), _stringsMap);
                    Iterator iterator1 = strings.elementIterator();
                    while (iterator1.hasNext()) {
                        Element string = (Element) iterator1.next();
                        //                System.out.println(string.getTextTrim());
                        string.setText(string.getText().replaceAll("  ", "").trim());
                        String name = _stringsMap.put(string.attributeValue("name"), string.getText());
                        if(name!=null)throw new Exception("重复的string："+string.attributeValue("name"));
                    }
                }
                Element words = languageElement.element("words");
                if (null != words) {
                    List<Word> _wordsList = new ArrayList<>();
                    StringFactory.wordMap.put(languageElement.attributeValue("language"), _wordsList);
                    Iterator wordIterator = words.elementIterator();
                    while (wordIterator.hasNext()) {
                        Element word = (Element) wordIterator.next();
                        _wordsList.add(new Word(word.element("word1").getText(), word.element("word2").getText()));
                        if(word.element("word1").getText().equals(word.element("word2").getText())) throw new Exception("词语一致："+word.element("word1").getText() );
                    }
                }
            }

            logger.info("StringFactory加载结束");
            logger.info("语言数:"+languageAliasMap.size());
            Iterator<String> iterator1 = languageAliasMap.keySet().iterator();
            while (iterator1.hasNext()){
                String next = iterator1.next();
                logger.info(next +":"+ languageAliasMap.get(next));
                logger.info("句："+ stringsMap.get(next).size());
                logger.info("词："+ wordMap.get(next).size());
            }
        }catch (Exception e){
            logger.error("StringFactory加载失败");
            e.printStackTrace();
        }
    }
}
