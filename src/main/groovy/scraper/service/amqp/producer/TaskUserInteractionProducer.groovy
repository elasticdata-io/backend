package scraper.service.amqp.producer

import groovy.json.JsonBuilder
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import scraper.service.amqp.dto.DisableUserInteractionDto

@Service
class TaskUserInteractionProducer {

    @Value('${spring.rabbitmq.exchange.inboxFanout}')
    String inboxFanoutExchangeName

    @Autowired
    private RabbitTemplate rabbitTemplate

    /**
     * @param taskId
     */
    void disableInteraction(DisableUserInteractionDto dto) {
        String dtoSerialized = new JsonBuilder(dto).toString()
        HashMap map = new HashMap(
                data: dtoSerialized,
                _type: 'disable_interaction_mode'
        )
        String message = new JsonBuilder(map).toString()
        rabbitTemplate.convertAndSend(inboxFanoutExchangeName, '', message)
    }
}

