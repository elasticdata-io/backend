package scraper.service.service.bot.telegram

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import scraper.service.service.PipelineService
import scraper.service.service.TelegramIncomingHookService
import scraper.service.service.UserService

@Component
class TelegramBot extends AbilityBot {

    @Autowired
    TelegramIncomingHookService telegramIncomingHookService

    @Autowired
    PipelineService pipelineService

    @Autowired
    UserService userService

    TelegramBot() {
        super("1407811086:AAGGlsGTG2vDclTbu5Dn2yx5LczMtpUOJ34", "ElasticDataBot")
    }

    @Override
    int creatorId() {
        return 1407811086
    }

    @Override
    protected boolean allowContinuousText() {
        return true
    }

    Ability start() {
        return Ability
                .builder()
                .name("start")
                .info("start")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action({ ctx ->
                    long chatId = ctx.chatId()
                    def arguments = ctx.arguments()
                    if (arguments.length < 2) {
                        sendCustomKeyboard(ctx.chatId() as String)
                        // silent.send('args userId is empty, todo please login in site...', ctx.chatId())
                        return
                    }
                    String userId = arguments[1]
                    if (!userId) {
                        silent.send('args userId is empty, todo please login in site...', ctx.chatId())
                        return
                    }
                    def user = telegramIncomingHookService.rememberUser(userId, chatId as String)
                    def message = """*${user.firstName} ${user.secondName}* you are welcome!
                                    |/commands - list all available commands""".stripMargin()
                    silent.sendMd(message, ctx.chatId())
                })
                .build()
    }

    Ability pipelineInfo() {
        return Ability
                .builder()
                .name("pipeline_info")
                .info("(id) information of pipeline")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action({ ctx ->
                    long chatId = ctx.chatId()
                    def arguments = ctx.arguments()
                    if (arguments.length == 0) {
                        return
                    }
                    String pipelineId = arguments[0]
                    if (!pipelineId) {
                        silent.send('args id is empty, nothing do...', ctx.chatId())
                        return
                    }
                    def pipeline = pipelineService.findById(pipelineId)
                    String link = "https://app.elasticdata.io/#/pipeline/edit/${pipelineId}"
                    def message = """Title: *${pipeline.key}*
                                    |Pipeline version: *${pipeline.pipelineVersion}*
                                    |Status: *${pipeline.status}*
                                    |Tasks total: *${pipeline.tasksTotal}*
                                    |Open in browser: [elasticdata.io](${link})
                                    |Last started on UTC: *${pipeline.lastStartedOn}*)
                                    |Last completed on UTC: *${pipeline.lastCompletedOn}*)
                                    |HookUrl: *${pipeline.hookUrl}*""".stripMargin()
                    silent.sendMd(message, ctx.chatId())
                })
                .build()
    }

    Ability listPipelines() {
        return Ability
                .builder()
                .name("pipelines")
                .info("list of available pipelines")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action({ ctx ->
                    long chatId = ctx.chatId()
                    def user = userService.findByTelegramId(ctx.chatId() as String)
                    def pipelines = pipelineService.findAll(user.id)
                    def list = pipelines
                            .collect{"${it.key} /pipeline_info${it.id}"}
                            .join('\n')
                    SendMessage sendMessage = new SendMessage()
                    sendMessage.setChatId(chatId as String)
                    sendMessage.setText(list)
                    silent.execute(sendMessage)
                })
                .build()
    }

    void sendCustomKeyboard(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Custom message text");

        // Create ReplyKeyboardMarkup object
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        // Create the keyboard (list of keyboard rows)
        List<KeyboardRow> keyboard = new ArrayList<>();
        // Create a keyboard row
        KeyboardRow row = new KeyboardRow();
        // Set each button, you can also use KeyboardButton objects if you need something else than text
        row.add("Row 1 Button 1");
        row.add("Row 1 Button 2");
        row.add("Row 1 Button 3");
        // Add the first row to the keyboard
        keyboard.add(row);
        // Create another keyboard row
        row = new KeyboardRow();
        // Set each button for the second line
        row.add("Row 2 Button 1");
        row.add("Row 2 Button 2");
        row.add("Row 2 Button 3");
        // Add the second row to the keyboard
        keyboard.add(row);
        // Set the keyboard to the markup
        keyboardMarkup.setKeyboard(keyboard);
        // Add it to the message
        message.setReplyMarkup(keyboardMarkup);
        execute(message)
    }

}
