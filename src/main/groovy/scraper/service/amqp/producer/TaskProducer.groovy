package scraper.service.amqp.producer

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import scraper.service.amqp.QueueConstants
import scraper.service.amqp.RoutingConstants

@Service
class TaskProducer {

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
        rabbitTemplate.convertAndSend(topicExchangeName, routingConstants.TASK_CHANGED, taskId)
    }
}
