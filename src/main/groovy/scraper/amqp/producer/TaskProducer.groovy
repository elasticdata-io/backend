package scraper.amqp.producer

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import scraper.amqp.RoutingConstants
import scraper.amqp.dto.ExecuteCommandDto
import scraper.model.Task
import scraper.proxy.ProxyAssigner
import scraper.service.PipelineService
import scraper.service.TariffPlanService

@Service
class TaskProducer {
    private Logger logger = LogManager.getRootLogger()

    @Value('${spring.rabbitmq.exchange.runTask}')
    private String runTaskExchangeName

    @Value('${spring.rabbitmq.exchange.inboxFanout}')
    String inboxFanoutExchangeName

    @Autowired
    RoutingConstants routingConstants

    @Autowired
    private RabbitTemplate rabbitTemplate

    @Autowired
    ProxyAssigner proxyAssigner

    @Autowired
    PipelineService pipelineService

    @Autowired
    TariffPlanService tariffPlanService

    /**
     * @param taskId
     */
    void taskRunNode(Task task) {
        def pipeline = pipelineService.findById(task.pipelineId)
        HashMap map = new HashMap(
            taskId: task.id,
            json: JsonOutput.toJson(task.dsl),
            userUuid: task.userId,
            pipelineId: task.pipelineId,
            proxies: [proxyAssigner.getProxy()],
            pipelineSettings: pipeline.dsl?.settings
        )
        def message = new JsonBuilder(map).toString()
        String workingType = getWorkerType(task.userId)
        logger.info("logger type is ${workingType}")
        String routingKey = "${routingConstants.TASK_RUN_ROUTING_KEY}.${workingType}"
        rabbitTemplate.convertAndSend(runTaskExchangeName, routingKey, message)
    }

    /**
     * @param taskId
     */
    void taskStopV2(String taskId) {
        HashMap map = new HashMap(
            data: taskId,
            _type: 'stop_task'
        )
        String message = new JsonBuilder(map).toString()
        rabbitTemplate.convertAndSend(inboxFanoutExchangeName, '', message)
    }

    /**
     * @param taskId
     */
    void taskChanged(String taskId) {}

    void executeCommand(ExecuteCommandDto dto) {
        String dtoSerialized = new JsonBuilder(dto).toString()
        HashMap map = new HashMap(
                data: dtoSerialized,
                _type: 'execute_cmd'
        )
        String message = new JsonBuilder(map).toString()
        rabbitTemplate.convertAndSend(inboxFanoutExchangeName, '', message)
    }

    private String getWorkerType(String userId) {
        def tariffPlan = tariffPlanService.getTariffPlanByUserId(userId)
        if (tariffPlan.configuration.privateWorkers > 0) {
            return userId
        }
        return 'A'
    }
}

