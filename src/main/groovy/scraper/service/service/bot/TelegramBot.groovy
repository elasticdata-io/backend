package scraper.service.service.bot

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
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
                        silent.send('args userId is empty, todo please login in site...', ctx.chatId())
                        return
                    }
                    String userId = arguments[1]
                    if (!userId) {
                        silent.send('args userId is empty, todo please login in site...', ctx.chatId())
                        return
                    }
                    def user = telegramIncomingHookService.rememberUser(userId, chatId as String)
                    def message = """Welcome! *${user.firstName} ${user.secondName}* are started!
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

}
