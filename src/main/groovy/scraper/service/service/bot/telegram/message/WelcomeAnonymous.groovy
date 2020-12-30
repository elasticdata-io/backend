package scraper.service.service.bot.telegram.message

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

@Component
class WelcomeAnonymous extends AbstractMessage {

    SendMessage getMessage() {
        SendMessage message = new SendMessage()
        def text = readMarkdown('en/welcome-anonymous.md')
        message.setText(text)
        message.setParseMode('MarkdownV2')
        return message
    }
}
