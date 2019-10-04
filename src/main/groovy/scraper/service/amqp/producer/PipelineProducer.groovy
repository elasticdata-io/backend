package scraper.service.amqp.producer

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import scraper.service.amqp.QueueConstants

@Service
class PipelineProducer {

    @Value('${spring.rabbitmq.topicExchangeName}')
    private String topicExchangeName

    @Autowired
    private RabbitTemplate rabbitTemplate

    void run(String pipelineId) {
        rabbitTemplate.convertAndSend(topicExchangeName, QueueConstants.PIPELINE_RUN, pipelineId)
    }

    void runHierarchy(List<String> pipelineIdList) {
        rabbitTemplate.convertAndSend(topicExchangeName, QueueConstants.PIPELINE_RUN_HIERARCHY, pipelineIdList)
    }

    void stop(String pipelineId) {
        rabbitTemplate.convertAndSend(topicExchangeName, QueueConstants.PIPELINE_STOP, pipelineId)
    }

}