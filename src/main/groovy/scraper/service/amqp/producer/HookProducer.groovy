package scraper.service.amqp.producer

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import scraper.service.amqp.RoutingConstants

@Service
class HookProducer {

    @Value('${spring.rabbitmq.exchange.runTask}')
    private String runTaskExchangeName

    @Autowired
    RoutingConstants routingConstants

    @Autowired
    private RabbitTemplate rabbitTemplate

    /**
     * @param taskId
     */
    void runHook(String taskId) {
        rabbitTemplate.convertAndSend(runTaskExchangeName, routingConstants.RUN_HOOK_ROUTING_KEY, taskId)
    }

}

