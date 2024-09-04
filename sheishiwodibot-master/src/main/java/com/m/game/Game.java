package com.m.game;

import com.m.StringFactory;
import com.m.bot.impl.TelegramBotImpl;
import com.m.service.ButtonEvent;
import com.m.sheishiwodi.word.Word;
import com.m.sql.entity.Telegram_group;
import com.m.sql.service.TelegramGroupService;
import com.m.sql.service.TelegramUserService;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class Game extends Thread{
    /** 发送加入游戏邀请时间间隔 */
    public final static long WaitingYoJoinTimeInterval = 1000*30;
    /** 最小开始游戏所需人数 */
    public final static long minMemberSize = 4;
    /** 超时关闭游戏时间 （仅在游戏未开始有效) */
    public final static long MaxActiveTime = 1000*40;
    /** 讨论时间每位玩家增加的游戏(秒数 )*/
    public final static long GameSecondsAddedByThePlayer = 20;
    /** 设置最大讨论时间上限 (秒数 )*/
    public final static long DiscussionTimeLimit = 150;
    /** 设置最大投票时间上限 */
    public final static long voteTimeLimit = 1000*60;
    /** 设置最大未投票自动淘汰上限 */
    public final static int notVote = 2;
    /** 淘汰所需的最大票数, */
    public final static int maximumVote = 4;
    /** 提醒发言倒计时 */
    public final static int voteReminderVote = 30000;
    /** 参与分数 */
    public final static int participationScore = 3;
    /** 平民获胜得分 */
    public final static int word_people_victory = 1;
    /** 每 8人卧底获胜得分 */
    public final static int word_spy_victory = 3;
    /** 淘汰扣分 */
    public final static int Deduction = 2;

    public long  endActiveTime;
    TelegramBotImpl telegramBot;
    public Long chatId;
    String language;
    final List<Member> memberList =new ArrayList<>();
    String  word_people;
    String  word_spy;
    Message sendInviteMessage;
    long sendInviteTime =0;
    Status status;
    Chat chat;
    public boolean run =true;
    boolean botIsAdmin =false;
    boolean updateInvitation =false;
    int rotate = 0 ;
    /** 讨论截止时间*/
    long speechTimeEnd;
    /** 投票截止时间*/
    long voteTimeEnd;;
    /** 即将开始投票提醒*/
    boolean voteReminder ;
    Game(Telegram_group group, Message message, TelegramBotImpl telegramBot){
        this.endActiveTime = System.currentTimeMillis();
        this.status = Status.dengdaijiaru;//等待加入
        this.chat = message.getChat();
        this.chatId =this.chat.getId();
        this.language = group.getLanguage() ;
        this.telegramBot = telegramBot;

        joinGame(message.getFrom());
        start();
    }
    public synchronized void joinGame(User user){
        TelegramUserService.updateUserData(user);
        if(null==getMember(user.getId())&&status==Status.dengdaijiaru)
        {
            synchronized(memberList){
                memberList.add(new Member(user));
            }
            TelegramUserService.upJoinGame(user);
            updateInvitation = true;
            endActiveTime = System.currentTimeMillis();
        }
    }

    @Override
    public void run() {
        long endTime;
        while (run&&status!=Status.close) {
            endTime = System.currentTimeMillis();
            if (status == Status.dengdaijiaru&&endTime-sendInviteTime>WaitingYoJoinTimeInterval) {
                sendInvite();
            }
            if(status==Status.dengdaijiaru&&endTime%5==0&& updateInvitation){
                editInvite();
                updateInvitation =false;
            }
            if(status == Status.dengdaijiaru&&endTime-endActiveTime>MaxActiveTime){
                if(!isAllMemberReady()) {
                    SendMessage sendMessage = new SendMessage(chatId.toString(), StringFactory.getTimeoutShutdown(language));
                    telegramBot.sendMessage(sendMessage);
                    status = Status.close;
                }else{
                    status = Status.taolunshijian;
                    SendMessage sendMessage = new SendMessage(chatId.toString(),
                            StringFactory.getGameStart(language));
                    telegramBot.sendMessage(sendMessage);
                    initWords();
                    sendUserWord();
                    sendSpeechPerform();
                }
            }
            if(status==Status.taolunshijian&&endTime>speechTimeEnd){
                status= Status.toupiaozhong;
                sendVote();
            }
            if(status==Status.taolunshijian &&!voteReminder&&endTime>(speechTimeEnd-voteReminderVote)){
                sendAboutToVote();
            }
            if(status==Status.toupiaozhong && (endTime > voteTimeEnd||isFinishVote())){
                //处理投票结果
                processVoteResult(!isFinishVote());
            }

            try {sleep(800);} catch (InterruptedException e) {e.printStackTrace();}
        }
        GameList.removeGame(this);
    }
    boolean isFinishVote(){
        for(Member member:memberList){
            if(member.survive&&!member.finishVote)return  false;
        }
        return true;
    }
    public synchronized Boolean vote(User user,Long toUser){
        boolean ret = false;
        Member member = getMember(user.getId());
        Member toMember = getMember(toUser);
        if(member!=null&&toMember!=null&&!member.finishVote&&member.survive&&toMember.survive){
            member.finishVote = true;
            toMember.beVoted++;
            member.toUser=toMember;
            ret =true;
        }
        if(toUser==-1&&member!=null&&!member.finishVote&&member.survive){
            member.finishVote = true;
            ret =true;
        }

        return ret;
    }
    /** 发送开始讨论*/
    void sendSpeechPerform(){
        rotate++;
        status = Status.taolunshijian;
        voteReminder= false;
        long SpeechTime = GameSecondsAddedByThePlayer*getSurvivesNumber();
        if(SpeechTime>DiscussionTimeLimit)
           SpeechTime=DiscussionTimeLimit;
        speechTimeEnd = System.currentTimeMillis()+(SpeechTime*1000);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(this.chatId.toString());
        sendMessage.setText(StringFactory.geSpeechTime(SpeechTime,rotate,language));
        sendMessage.setReplyMarkup(TelegramBotImpl.getViewWord(language));
        sendInviteMessage = telegramBot.sendMessage(sendMessage,0,Status.taolunshijian,this);
        sendInviteTime = System.currentTimeMillis();
        for(Member member:memberList)if(member.survive)member.speak=false;
    }
    void sendInvite(){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(this.chatId.toString());
        sendMessage.setText(StringFactory.getGamePlayerWaiting(language)+
                getUserNames(true));
        sendMessage.setReplyMarkup(TelegramBotImpl.getJoinGameMarkup(language,memberList.size()>=minMemberSize));
        sendInviteMessage = telegramBot.sendMessage(sendMessage,WaitingYoJoinTimeInterval,
                Status.dengdaijiaru,this);
        sendInviteTime = System.currentTimeMillis();
    }
    public synchronized void editInvite(){
        if(sendInviteMessage!=null){
            telegramBot.editMessage(sendInviteMessage,
                    StringFactory.getGamePlayerWaiting(language)+
                            getUserNames(true)
                    ,TelegramBotImpl.getJoinGameMarkup(language,memberList.size()>=minMemberSize));
        }
    }
    public synchronized Member getMember(@NotNull Long userId) {

        try {
            for (int i = 0; i< memberList.size(); i++){
                Member member = memberList.get(i);
                if(member!=null&& member.getId().equals(userId)) return member;
            }
        }catch (Exception e){e.printStackTrace();}

        return null;
    }
    public synchronized int getSurvivesNumber() {
        int number = 0;
        try {
            for (int i = 0; i< memberList.size(); i++){
                Member member = memberList.get(i);
                if(member.survive)number++;
            }
        }catch (Exception e){e.printStackTrace();}
        return number;
    }
    public synchronized int getUndercoverSurvivesNumber() {
        int number = 0;
        try {
            for (int i = 0; i< memberList.size(); i++){
                Member member = memberList.get(i);
                if(member.survive && member.isUndercover)number++;
            }
        }catch (Exception e){e.printStackTrace();}
        return number;
    }
    public synchronized int getNotUndercoverSurviveNumber() {
        int number = 0;
        try {
            for (int i = 0; i< memberList.size(); i++){
                Member member = memberList.get(i);
                if(member.survive && !member.isUndercover)number++;
            }
        }catch (Exception e){e.printStackTrace();}
        return number;
    }
    boolean isAllMemberReady(){
        boolean ret = true;
        for (int i = 0; i<memberList.size(); i++){
            Member member = memberList.get(i);
            if (!member.ready) {
                ret = false;
                break;
            }
        }
        return ret;
    }
    public synchronized String getUserNames(boolean readys) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            for (int i = 0; i< memberList.size(); i++){
                if(i!=0)stringBuilder.append("、");
                Member member = memberList.get(i);
                stringBuilder.append(TelegramBotImpl.Name(member.getUser()));
                if(readys&&member.ready) stringBuilder.append("(").append(StringFactory.getReady(language)).append(")");
            }
        }catch (Exception e){e.printStackTrace();}

        return stringBuilder.toString();
    }
    public synchronized String getSurvivesUserNames() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean dian = false;
        try {
            for (Member member : memberList) {
                if (member.survive) {
                    if (dian) stringBuilder.append("、");
                    dian = true;
                    stringBuilder.append(TelegramBotImpl.Name(member.getUser()));
                }
            }
        }catch (Exception e){e.printStackTrace();}

        return stringBuilder.toString();
    }
    public String getLanguage() {
        return language;
    }

    public Status getStatus() {
        return status;
    }
    void initWords(){
        Word word = StringFactory.getWord(language);
        boolean b = System.currentTimeMillis()/2==0;
        if(word==null)return;
        word_people = b?word.getWord1():word.getWord2();
        word_spy = b?word.getWord2():word.getWord1();
        int size = memberList.size();
        Member member1 = memberList.get((int) (System.currentTimeMillis() % size));
        member1.word = word_spy;
        member1.isUndercover = true;
        for (int i = 8;i<= size; i += 8) {
            Member member = memberList.get((int) ((System.currentTimeMillis() * 7) % size));
            member.word = word_spy;
            member.isUndercover = true;
        }
        for (Member member: memberList){if(member.word==null)member.word=word_people; }
    }
    void sendUserWord(){
        for (Member member: memberList){
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(member.id.toString());
            sendMessage.setText(StringFactory.getSendWord(chat.getTitle(),
                    member.word,
                    language));
            Message message = telegramBot.sendMessage(sendMessage);
            if(message==null){
                GameList.UnsentUserWord unsentUserWord =new GameList.UnsentUserWord(StringFactory.getSendWord(chat.getTitle(),
                        member.word,
                        language),System.currentTimeMillis(),member.id,this);
                GameList.addUnsentUserWords(unsentUserWord);
            }
        }
    }

    void processVoteResult(boolean timeEnd){
        StringBuilder stringBuilder = new StringBuilder();
        if(timeEnd)
            stringBuilder.append(StringFactory.getVotedTimeEnd(language)).append("\n");
        else
            stringBuilder.append(StringFactory.getEveryoneVoted(language)).append("\n");
        for (Member member: memberList){//投给谁
            if(member.survive&&member.toUser!=null){
                stringBuilder.append(TelegramBotImpl.Name(member.getUser()))
                        .append("\uD83D\uDC49")
                        .append(TelegramBotImpl.Name(member.toUser.getUser()))
                        .append("\n");
                member.notVote=0;
            }
        }
        for (Member member: memberList){//放弃投
            if(member.survive&&member.toUser==null&&member.finishVote){
                stringBuilder.append(StringFactory.getAbstained(TelegramBotImpl.Name(member.getUser()),language))
                        .append("\n");
                member.notVote=0;
            }
        }
        for (Member member: memberList){//没有在时间内投票
            if(member.survive&&!member.finishVote){
                stringBuilder.append(StringFactory.getNotVote(TelegramBotImpl.Name(member.getUser()),language))
                        .append("\n");
                member.notVote++;
            }
        }
        int i = 0;
        int survivesNumber = getSurvivesNumber();
        //本轮淘汰所需票数
        int weedOut = survivesNumber/2+(survivesNumber%2>0?1:0);
        boolean start = true;
        for (Member member: memberList){//淘汰
            if(member.survive){
                if(member.beVoted >= Game.maximumVote||member.beVoted >= weedOut||member.notVote>=notVote){
                    member.survive = false;
                    if(start){start=false;
                        stringBuilder.append(StringFactory.getEliminatedInThisRound(language));
                        stringBuilder.append(TelegramBotImpl.Name(member.getUser()));
                    }else {
                        stringBuilder.append("、").append(TelegramBotImpl.Name(member.getUser()));
                    }

                }
            }
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(stringBuilder.toString());
        telegramBot.sendMessage(sendMessage,DiscussionTimeLimit*1000);
        //判断游戏结束
        if(getUndercoverSurvivesNumber()>=getNotUndercoverSurviveNumber()||getUndercoverSurvivesNumber()==0){
            if(getUndercoverSurvivesNumber()>=getNotUndercoverSurviveNumber()){
                for (Member member: memberList){//淘汰剩余平民
                    if(member.survive&&!member.isUndercover){
                        member.survive = false;
                    }
                }
            }
           sendGameOver();
        }else {
            SendMessage sendMessage1 = new SendMessage();
            sendMessage1.setChatId(chatId.toString());
            sendMessage1.setText(StringFactory.getRemainingPersonnel(language,getSurvivesNumber(),memberList.size())+"\n"+
                    getSurvivesUserNames()
                    );
            telegramBot.sendMessage(sendMessage1,DiscussionTimeLimit*1000);
            sendSpeechPerform();
        }
    }
    void sendGameOver(){
        StringBuilder stringBuilder = new StringBuilder();
        status = Status.settlement;
        stringBuilder.append(StringFactory.getGameOver(language)).append("\n");
        for (Member member: memberList){
            if(member.survive) {
                stringBuilder.append(StringFactory.getUserWordIs(
                        language, TelegramBotImpl.Name(member.user), member.word));
                stringBuilder.append(member.isUndercover?"\uD83E\uDD21":"\uD83D\uDE4D\u200D♂️").append("\uD83C\uDFC6\n");
            }
        }
        for (Member member: memberList){//淘汰
            if(!member.survive) {
                stringBuilder.append(StringFactory.getUserWordIs(
                        language, "<s>"+TelegramBotImpl.Name(member.user)+"</s>", member.word));
                stringBuilder.append(member.isUndercover?"\uD83E\uDD21":"\uD83D\uDE4D\u200D♂️").append("☠️\n");
            }
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(stringBuilder.toString());
        telegramBot.sendMessage(sendMessage);

        try{
            TelegramGroupService.upFinisgGame(this);
            TelegramGroupService.upMaxOfPeople(this,memberList.size());
            String[] completeGame_id = new String[memberList.size()];
            List<String> word_people_id = new ArrayList<>();
            List<String> word_spy_id = new ArrayList<>();
            List<String> word_people_victory_id = new ArrayList<>();
            List<String> word_spy_victory_id = new ArrayList<>();
            for (int i = 0; i < memberList.size(); i++) {
                Member member = memberList.get(i);
                completeGame_id[i]= member.getUser().getId().toString();
                member.fraction+=participationScore;
                if(!member.survive) member.fraction -= Deduction;
                if(!member.isUndercover){
                    word_people_id.add(member.user.getId().toString());
                }
                if(member.isUndercover){
                    word_spy_id.add(member.user.getId().toString());
                }
                if(!member.isUndercover&&member.survive){
                    word_people_victory_id.add(member.user.getId().toString());
                    member.fraction+=word_people_victory;
                }
                if(member.isUndercover&&member.survive){
                    word_spy_victory_id.add(member.user.getId().toString());
                    member.fraction+=word_spy_victory*(1+memberList.size()/8);
                }
                TelegramUserService.upFraction(member.user.getId().toString(),member.fraction);
            }
            TelegramUserService.newCompleteGame(completeGame_id);
            TelegramUserService.newWord_people(word_people_id.toArray(new String[0]));
            TelegramUserService.newWord_spy(word_spy_id.toArray(new String[0]));
            TelegramUserService.newWord_people_victory(word_people_victory_id.toArray(new String[0]));
            TelegramUserService.newWord_spy_victory(word_spy_victory_id.toArray(new String[0]));

        }catch (Exception e){e.printStackTrace();}

        status = Status.close;
    }
    public void speak(Long userId){
        if(status == Status.taolunshijian){
            Member member = getMember(userId);
            if(member!=null&&!member.speak&&member.survive)member.speak =true;
        }
    }
    public void memberReady(Long userId ){
        Game.Member member = getMember(userId);
        if (null != member && !member.ready) {
            member.ready = true;
            endActiveTime = System.currentTimeMillis();
            updateInvitation = true;
        }
    }
    void sendVote(){
        voteTimeEnd = System.currentTimeMillis()+voteTimeLimit;
        for (Member member:memberList){
            if(member.survive) {
                member.finishVote = false;
                member.beVoted = 0;
                member.toUser = null;
            }
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(StringFactory.getVotingStart(language));
        sendMessage.setReplyMarkup(getVoteMarkup());
        telegramBot.sendMessage(sendMessage,0,Status.toupiaozhong,this);
    }
    void sendAboutToVote(){
        voteReminder=true;
        boolean someoneSpeaks = false;
        int speaks = 0;
        StringBuilder stringBuilder =new StringBuilder();
        for (Member member : memberList) {
            if (member.survive && member.speak) {
                someoneSpeaks = true;
                break;
            }
        }
        if(someoneSpeaks){
            stringBuilder.append(StringFactory.getAboutToVoteL(language));
            boolean b = false;
            for (Member member : memberList) {
                if (member.survive && !member.speak) {
                    if (b) stringBuilder.append("、");
                    if (!b)b = true;
                    speaks++;
                    stringBuilder.append(TelegramBotImpl.Name(member.getUser()));
                }
            }
            stringBuilder.append(StringFactory.getAboutToVoteR(language));
        }else {
            stringBuilder.append(StringFactory.getNotAdmin(language));
        }
        if(someoneSpeaks&&speaks==0)return;
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(stringBuilder.toString());
        telegramBot.sendMessage(sendMessage,voteReminderVote);
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    InlineKeyboardMarkup getVoteMarkup(){

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Member member: memberList){
            if(member.survive){
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                JSONObject data = new JSONObject();
                data.put("action", ButtonEvent.vote);
                data.put("to",member.id);
                button.setText(TelegramBotImpl.NameText(member.getUser()));
                button.setCallbackData(data.toString());
                rowInline.add(button);
                rows.add(rowInline);
            }
        }
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        JSONObject data = new JSONObject();
        data.put("action", ButtonEvent.vote);
        data.put("to",-1);
        button.setText(StringFactory.getAbstain(language));
        button.setCallbackData(data.toString());
        rowInline.add(button);
        rows.add(rowInline);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
    public void setLanguage(String language) {
        this.language = language;
    }
    public static class Member{
        User user;
        Long id ;
        String word;

        /** 被投票
         */
        public int beVoted =0;
        /** 完成投票
         */
        public boolean finishVote ;
        /**是卧底
         */
        public boolean isUndercover ;
        /**准备
         */
        public boolean ready = false;
        /** 存活
         */
        public boolean survive = true;
        /** 没有投票
         */
        public int notVote = 0;
        /** 投票给
         */
        Member toUser;
        /** 游戏结算分
         */
        public int fraction = 0;

        public boolean speak =false;
        public Long getId() {
            return id;
        }

        public User getUser() {
            return this.user;
        }

        public Member(User user){
            this.user = user;
            this.id=user.getId();
        }
    }

}