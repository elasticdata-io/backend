package scraper.service.amqp

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class QueueConstants {

    @Value('${spring.rabbitmq.queue.runHooks}')
    String RUN_HOOKS
}
