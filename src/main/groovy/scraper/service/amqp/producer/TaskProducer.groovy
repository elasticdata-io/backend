package scraper.service.amqp.producer

import groovy.json.JsonBuilder
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import scraper.service.amqp.RoutingConstants
import scraper.service.model.Task
import scraper.service.proxy.ProxyAssigner

@Service
class TaskProducer {

    private static Logger logger = LogManager.getLogger(TaskProducer.class)

    @Value('${spring.rabbitmq.topicExchangeName}')
    private String topicExchangeName

    @Autowired
    RoutingConstants routingConstants

    @Autowired
    private RabbitTemplate rabbitTemplate

    @Autowired
    ProxyAssigner proxyAssigner

    /**
     * @param taskId
     */
    void taskRun(String taskId) {
        logger.info("TaskProducer.taskRun taskId = ${taskId}")
        rabbitTemplate.convertAndSend(topicExchangeName, routingConstants.PIPELINE_TASK_RUN, taskId)
    }

    /**
     * @param taskId
     */
    void taskRunNode(Task task) {
        logger.info("TaskProducer.taskRunNode taskId = ${task.id}")
        HashMap map = new HashMap(
            taskId: task.id,
            json: task.commands,
            userUuid: task.userId,
            proxy: proxyAssigner.getProxy()
        )
        def message = new JsonBuilder(map).toString()
        rabbitTemplate.convertAndSend(topicExchangeName, routingConstants.PIPELINE_TASK_RUN_NODE, message)
    }

    /**
     * @param taskId
     */
    void taskStop(String taskId) {
        rabbitTemplate.convertAndSend(topicExchangeName, routingConstants.PIPELINE_TASK_STOP, taskId)
    }

    /**
     * @param taskId
     */
    void taskFinish(String taskId) {
        rabbitTemplate.convertAndSend(topicExchangeName, routingConstants.PIPELINE_TASK_FINISH, taskId)
    }

    /**
     * @param taskId
     */
    void taskChanged(String taskId) {
        logger.info("TaskProducer.taskChanged taskId = ${taskId}")
        rabbitTemplate.convertAndSend(topicExchangeName, routingConstants.TASK_CHANGED, taskId)
    }
}
