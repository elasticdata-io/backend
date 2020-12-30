package scraper.service.service.bot.telegram.message

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@Component
class Menu extends AbstractMessage {

    SendMessage mainMenuButton(String text) {
        SendMessage message = new SendMessage()
        message.setText(text)
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup()
        List<KeyboardRow> keyboard = new ArrayList<>()
        KeyboardRow row = new KeyboardRow()
        row.add("menu")
        keyboard.add(row)
        keyboardMarkup.setKeyboard(keyboard)
        message.setReplyMarkup(keyboardMarkup)
        message.setParseMode('MarkdownV2')
        return message
    }

    SendMessage mainMenu(String text) {
        SendMessage message = new SendMessage()
        message.setText(text)
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup()
        List<InlineKeyboardButton> row1 = new ArrayList<>()
        def pipelinesBtn = new InlineKeyboardButton()
        pipelinesBtn.setText("Show all pipelines")
        pipelinesBtn.setCallbackData("/pipelines")
        row1.add(pipelinesBtn)
        keyboardMarkup.setKeyboard([row1])
        message.setReplyMarkup(keyboardMarkup)
        message.setParseMode('Markdown')
        return message
    }
}
