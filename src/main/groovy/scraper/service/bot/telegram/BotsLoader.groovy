package scraper.service.bot.telegram

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import javax.annotation.PostConstruct

@Component
class BotsLoader {

    @Value('${bot.telegram.enabled}')
    Boolean enabled

    @Autowired
    TelegramBot telegramBot

    @PostConstruct
    void init() {
        if (!enabled) {
            return
        }
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class)
            botsApi.registerBot(telegramBot)
        } catch (TelegramApiException e) {
            e.printStackTrace()
        }
    }
}
