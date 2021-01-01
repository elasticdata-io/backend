package scraper.amqp.producer

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import scraper.amqp.RoutingConstants
import scraper.amqp.dto.ExecuteCommandDto
import scraper.model.Task
import scraper.proxy.ProxyAssigner
import scraper.service.PipelineService

@Service
class TaskProducer {

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
        String workingType = 'A'
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
}
