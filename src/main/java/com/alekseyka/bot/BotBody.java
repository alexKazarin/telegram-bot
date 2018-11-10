package com.alekseyka.bot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import static com.alekseyka.bot.BotConfigs.*;

public class BotBody extends TelegramLongPollingBot {

    private Logger logger;
    private String oldForumMsg = "initialize", newForumMsg = "initialize";
    private Map<Long,Boolean> listOfChats = new HashMap<>();

    public BotBody(DefaultBotOptions botOptions) {
        super(botOptions);
        logger = Logger.getLogger("BotLog");
    }

    public void mainThread() throws InterruptedException {
        while (true) {
            if (!newForumMsg.equals(oldForumMsg)) {
                notificateAllChannels();
                oldForumMsg = newForumMsg;
            }
            int sleepPeriod = 20000 + ThreadLocalRandom.current().nextInt(0, 5000);
            logger.info("Sleep for " + sleepPeriod + "ms");
            Thread.sleep(sleepPeriod);
            newForumMsg = checkForum();
        }
    }

    public void notificateAllChannels() {
        for (Map.Entry<Long,Boolean> chat : listOfChats.entrySet()) {
            if (chat.getValue()) {
                sendMessage(newForumMsg, chat.getKey());
            }
        }
    }

    private void sendMessage(String messageText, Long chatId) {
        SendMessage message = new SendMessage()
                .setChatId(chatId)
                .setText(messageText);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String checkForum() {
        Document doc = null;
        String lastMsgFromForum;
        try {
            doc = Jsoup.connect(FORUM_PAGE).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        lastMsgFromForum = (doc != null) ? doc.select(FORUM_MSG_JSOUP).last().text() : "ERROR_PARSE";
        logger.info("Regular message check: " + lastMsgFromForum);
        return lastMsgFromForum;
    }


    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = update.getMessage().getChatId();
        Boolean chatSubscribed;

        if (update.hasMessage() && update.getMessage().hasText()) {
            String receivedMsg = update.getMessage().getText();
            String responseMsg;
            String defaultCommandsMsg = "Available commands:\n" +
                    "/on - start notifications;\n" +
                    "/off - stop notifications;\n" +
                    "/ping - receive last message from forum;\n" +
                    "/help - available commands;\n";

            logger.info("Received command: " + receivedMsg);
            switch (receivedMsg) {
                case "/start": {
                    chatSubscribed = true;
                    listOfChats.put(chatId, chatSubscribed);
                    responseMsg = "Welcome! This is direct notification from Vinski Forum.\n" +
                            defaultCommandsMsg;
                    break;
                }
                case "/on": {
                    chatSubscribed = true;
                    listOfChats.put(chatId, chatSubscribed);
                    responseMsg = "You subscribed";
                    break;
                }
                case "/off": {
                    chatSubscribed = false;
                    listOfChats.put(chatId, chatSubscribed);
                    responseMsg = "You unsubscribed";
                    break;
                }
                case "/ping": {
                    responseMsg = newForumMsg;
                    break;
                }
                case "/help": {
                    responseMsg = defaultCommandsMsg;
                    break;
                }
                default:
                    responseMsg = "Don't understand you";
                    break;
            }
            sendMessage(responseMsg, chatId);
        }
    }

    @Override
    public String getBotUsername() {
        // TODO
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        // TODO
        return BOT_TOKEN;
    }
}
