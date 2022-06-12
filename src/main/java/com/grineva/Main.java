package com.grineva;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;


public class Main {
    private static final String TOKEN="5442707370:AAH6UlBOU-U-_GaAYp04gd3LEnH4E3aBgJs";
    private static final ConcurrentHashMap <PomodoroBot.Timer, Long> userTimers=new ConcurrentHashMap();
    public static void main(String[] args) throws TelegramApiException {

        TelegramBotsApi telegramBotsApi=new TelegramBotsApi(DefaultBotSession.class);
        PomodoroBot bot=new PomodoroBot();
        telegramBotsApi.registerBot(bot);
        new Thread(()-> {
            try {
                bot.checkTimer();
            } catch (InterruptedException e) {
                System.out.println("Упппс");
            }
        }).run();

    }
    static class PomodoroBot extends TelegramLongPollingBot{
        enum TimerType{
            WORK,
            BREAK
        }

        static class Timer{
            Instant time;
            TimerType timerType;

            public Instant getTime() {
                return time;
            }

            public void setTime(Instant time) {
                this.time = time;
            }

            public TimerType getTimerType() {
                return timerType;
            }

            public void setTimerType(TimerType timerType) {
                this.timerType = timerType;
            }

            public Timer(Instant time, TimerType timerType) {
                this.time = time;
                this.timerType = timerType;
            }
        }



        @Override
        public String getBotUsername() {
            return "Pomodoro bot";
        }

        @Override
        public String getBotToken() {
            return TOKEN;
        }

        @Override
        public void onUpdateReceived(Update update) {
            if(update.hasMessage()&&update.getMessage().hasText()){
                Long chatId=update.getMessage().getChatId();
                if(update.getMessage().getText().equals("/start")){
                    sendMsg("Pomodoro-сделай свое время более эффективным\n" +
                            "Задай мне время работы и отдыха через пробел. Например, '1 1'.\n" +
                            "Я работаю в минутах", update.getMessage().getChatId().toString());
                }
                else {
                    var args=update.getMessage().getText().split(" ");
                    if(args.length>=1){
                        var worktime=Instant.now().plus(Long.parseLong(args[0]), ChronoUnit.MINUTES);
                        userTimers.put(new Timer(worktime, TimerType.WORK), update.getMessage().getChatId());
                        sendMsg("Давай работай!", chatId.toString());
                        if(args.length>=2){
                            var breakTime=worktime.plus(Long.parseLong(args[1]), ChronoUnit.MINUTES);
                            userTimers.put(new Timer(breakTime, TimerType.BREAK), update.getMessage().getChatId());
                        }
                    }

                }
            }

        }
        public void checkTimer() throws InterruptedException {
            while (true){
                System.out.println("Количество таймеров пользователей = "+userTimers.size());
                userTimers.forEach((timer, userId)->{
                    System.out.printf("Проверка serId= %d, server time = %s, user timer = %s", userId, Instant.now().toString(), timer.time.toString());
                    if(Instant.now().isAfter(timer.time)){
                        userTimers.remove(timer);
                        switch (timer.timerType){
                            case WORK:sendMsg("Пора отдыхать!", userId.toString());
                            break;
                            case BREAK:sendMsg("Таймер завершил свою работу", userId.toString());
                            break;
                        }
                    }
                });
                Thread.sleep(1000);
            }
        }
        private void sendMsg(String text, String chatId) {
            SendMessage msg=new SendMessage();
            msg.setChatId(chatId);
            msg.setProtectContent(true);
            msg.setText(text);

            try {
                execute(msg);
            } catch (TelegramApiException e) {
                System.out.println("Уппс");
            }
        }
    }


    static class EchoBot extends TelegramLongPollingBot{

        @Override
        public String getBotUsername() {
            return "попугай bot";
        }

        @Override
        public String getBotToken() {
            return TOKEN;
        }


//Обработка входящих собщений
        @Override
        public void onUpdateReceived(Update update) {
            int userCount=0;
            if(update.hasMessage()&&update.getMessage().hasText()){
                if(update.getMessage().getText().equals("/start")){
                    userCount+=1;
                    System.out.println("Новый пользователь "+ userCount);
                    //приветсивие
                    sendMsg("Я попугай бот",
                            update.getMessage().getChatId().toString());
                }else {
                    System.out.println("Обработка сообщений");
                    sendMsg(update.getMessage().getText(),
                            update.getMessage().getChatId().toString());
                }
            }

        }

        private void sendMsg(String text, String chatId) {
            SendMessage msg=new SendMessage();
            msg.setChatId(chatId);
            msg.setProtectContent(true);
            msg.setText(text);

            try {
                execute(msg);
            } catch (TelegramApiException e) {
                System.out.println("Уппс");
            }
        }
    }
}
