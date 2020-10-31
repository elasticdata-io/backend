package scraper.service.amqp.producer

import groovy.json.JsonBuilder
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import scraper.service.amqp.RoutingConstants
import scraper.service.amqp.dto.ExecuteCommandDto
import scraper.service.model.Task
import scraper.service.proxy.ProxyAssigner
import scraper.service.service.PipelineService

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
            json: task.commands,
            userUuid: task.userId,
            pipelineId: task.pipelineId,
            proxies: pipeline.needProxy ? [proxyAssigner.getProxy()]: [],
            pipelineSettings: pipeline.pipelineConfiguration?.settings
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

