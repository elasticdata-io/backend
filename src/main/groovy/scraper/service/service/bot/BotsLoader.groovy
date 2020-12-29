package scraper.service.service.bot

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import javax.annotation.PostConstruct

@Component
class BotsLoader {

    @Autowired
    TelegramBot telegramBot

    @PostConstruct
    void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class)
            botsApi.registerBot(telegramBot)
        } catch (TelegramApiException e) {
            e.printStackTrace()
        }
    }
}
