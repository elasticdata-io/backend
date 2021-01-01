package scraper.service.bot.telegram.message

import groovy.text.SimpleTemplateEngine
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import scraper.model.User
import scraper.service.TelegramIncomingHookService

class WelcomeUserDto {
    String firstName
    String lastName
}

@Component
class WelcomeUser extends AbstractMessage {

    @Autowired
    TelegramIncomingHookService telegramIncomingHookService

    @Autowired
    Menu menu

    User rememberUser(MessageContext ctx) {
        long chatId = ctx.chatId()
        def arguments = ctx.arguments()
        if (arguments.length < 2) {
            return
        }
        String userId = arguments[1]
        return telegramIncomingHookService.rememberUser(userId, chatId as String)
    }

    SendMessage getMessage(WelcomeUserDto bindings) {
        def text = readMarkdown('en/welcome-user.md')
        def engine = new SimpleTemplateEngine()
        def template = engine
                .createTemplate(text)
                .make([*:extractProperties(bindings)])
        return menu.mainMenuButton(template.toString())
    }
}
