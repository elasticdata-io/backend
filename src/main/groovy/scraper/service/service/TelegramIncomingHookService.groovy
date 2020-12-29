package scraper.service.service

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.service.dto.model.telegram.IncomingHookDto

@Service
class TelegramIncomingHookService {

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    UserService userService

    void handleIncomingHook(IncomingHookDto dto) {
        String text = dto.message.text
        if (text.startsWith('/start')) {
            String userId = text.replace('/start ', '')
            rememberUser(userId, dto.message.chat.id)
        }
    }

    private void rememberUser(String userId, String chatId) {
        def user = userService.findById(userId)
        if (!user) {
            logger.error("Failed telegram chat id. id with id: ${userId} not found")
            return
        }
        user.telegramChatId = chatId
        userService.save(user)
        logger.info("remember telegram chat id for user: ${userId}")
    }
}
