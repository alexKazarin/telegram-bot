package com.alekseyka.main;

import com.alekseyka.bot.BotBody;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static com.alekseyka.bot.BotConfigs.PROXY_HOST;
import static com.alekseyka.bot.BotConfigs.PROXY_PORT;


public class App
{
    // The log file size is set to 10MB.
    private static final int FILE_SIZE = 10 * 1024 * 1024;

    public static void main( String[] args ) {
        try {
            Logger logger = Logger.getLogger("BotLog");
            FileHandler fh;
            fh = new FileHandler("./logs/bot.log", FILE_SIZE, 1, true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.info("Starting app");

            logger.info("ApiContextInitializer started");
            ApiContextInitializer.init();
            logger.info("ApiContextInitializer finished");

            logger.info("TelegramBotsApi started");
            TelegramBotsApi botsApi = new TelegramBotsApi();
            logger.info("TelegramBotsApi finished");

            DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

            botOptions.setProxyHost(PROXY_HOST);
            botOptions.setProxyPort(PROXY_PORT);
            // Select proxy type: [HTTP|SOCKS4|SOCKS5] (default: NO_PROXY)
            botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);

            // Register your newly created AbilityBot
            logger.info("BotBody register started");
            BotBody bot = new BotBody(botOptions);
            botsApi.registerBot(bot);
            logger.info("BotBody register finished");

            logger.info("BotBody started");
            bot.mainThread();

        } catch (TelegramApiException | InterruptedException | IOException e) {
            e.printStackTrace();
        }

    }
}
