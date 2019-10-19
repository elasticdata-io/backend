package scraper.service.amqp.producer

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import scraper.service.amqp.QueueConstants
import scraper.service.amqp.RoutingConstants

@Service
class PipelineProducer {

    @Value('${spring.rabbitmq.topicExchangeName}')
    private String topicExchangeName

    @Autowired
    QueueConstants queueConstants

    @Autowired
    RoutingConstants routingConstants

    @Autowired
    private RabbitTemplate rabbitTemplate

    void run(String pipelineId) {
        rabbitTemplate.convertAndSend(topicExchangeName, queueConstants.PIPELINE_RUN, pipelineId)
    }

    void runHierarchy(List<String> pipelineIdList) {
        rabbitTemplate.convertAndSend(topicExchangeName, queueConstants.PIPELINE_RUN_HIERARCHY, pipelineIdList)
    }

    void stop(String pipelineId) {
        rabbitTemplate.convertAndSend(topicExchangeName, queueConstants.PIPELINE_STOP, pipelineId)
    }

    void finish(String pipelineId) {
        rabbitTemplate.convertAndSend(topicExchangeName, routingConstants.PIPELINE_FINISH, pipelineId)
    }
}
