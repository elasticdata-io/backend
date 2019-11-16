package scraper.service.amqp.producer

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import scraper.service.amqp.RoutingConstants

@Service
class TaskProducer {
    private Logger logger = LogManager.getRootLogger()

    @Value('${spring.rabbitmq.topicExchangeName}')
    private String topicExchangeName

    @Autowired
    RoutingConstants routingConstants

    @Autowired
    private RabbitTemplate rabbitTemplate

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
