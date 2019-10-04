package scraper.service.amqp.producer

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import scraper.service.amqp.QueueConstants

@Service
class PipelineTaskProducer {

    @Value('${spring.rabbitmq.topicExchangeName}')
    private String topicExchangeName

    @Autowired
    private RabbitTemplate rabbitTemplate

    void taskFinish(String pipelineTaskId) {
        rabbitTemplate.convertAndSend(topicExchangeName, QueueConstants.PIPELINE_TASK_FINISH, pipelineTaskId)
    }
}
