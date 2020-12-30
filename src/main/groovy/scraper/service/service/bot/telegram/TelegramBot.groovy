package scraper.service.service.bot.telegram

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import scraper.service.service.PipelineService
import scraper.service.service.TaskService
import scraper.service.service.UserService
import scraper.service.service.bot.telegram.message.Menu
import scraper.service.service.bot.telegram.message.PipelineInformation
import scraper.service.service.bot.telegram.message.WelcomeAnonymous
import scraper.service.service.bot.telegram.message.WelcomeUser
import scraper.service.service.bot.telegram.message.WelcomeUserDto

@Component
class TelegramBot extends AbilityBot {

    @Autowired
    Menu menu

    @Autowired
    PipelineInformation pipelineInformation

    @Autowired
    WelcomeUser welcomeUser

    @Autowired
    WelcomeAnonymous welcomeAnonymous

    @Autowired
    TaskService taskService

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

    Ability menu() {
        return Ability
                .builder()
                .name("menu")
                .info("menu")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action({ ctx ->
                    doMenu(ctx)
                })
                .build()
    }

    private void doMenu(MessageContext ctx) {
        SendMessage message = menu.mainMenu('Well, today I have this menu:')
        message.setChatId(ctx.chatId() as String)
        silent.execute(message)
    }

    Ability start() {
        return Ability
                .builder()
                .name("start")
                .info("start")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action({ ctx ->
                    doStart(ctx)
                })
                .build()
    }

    private void doStart(MessageContext ctx) {
        def user = welcomeUser.rememberUser(ctx)
        SendMessage message = welcomeAnonymous.getMessage()
        if (user) {
            message = welcomeUser.getMessage(new WelcomeUserDto(
                    firstName: user.firstName ?: 'Guest',
                    lastName: user.secondName ?: ''
            ))
        }
        message.setChatId(ctx.chatId() as String)
        silent.execute(message)
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
                    def task = taskService.findLastCompletedTask(pipelineId)
                    def message = pipelineInformation.getMessage(pipeline, task)
                    message.setChatId(chatId as String)
                    silent.execute(message)
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
                    doPipelines(ctx)
                })
                .build()
    }

    private void doPipelines(MessageContext ctx) {
        long chatId = ctx.chatId()
        def user = userService.findByTelegramId(ctx.chatId() as String)
        def pipelines = pipelineService.findAll(user.id)
        def list = pipelines
                .collect { "${it.key} /pipeline_info${it.id}" }
                .join('\n')
        SendMessage sendMessage = new SendMessage()
        sendMessage.setChatId(chatId as String)
        sendMessage.setText(list)
        silent.execute(sendMessage)
    }

    Ability all() {
        return Ability.builder()
                .name(DEFAULT)
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .input(0)
                .action({ctx ->
                    def update = ctx.update()
                    def command = update.callbackQuery
                        ? update.callbackQuery.data
                        : update.message.text
                    if (command == '/pipelines') {
                        return doPipelines(ctx)
                    }
                    if (command == 'menu') {
                        return doMenu(ctx)
                    }
                })
                .build()
    }
}
