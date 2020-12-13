package scraper.service.amqp

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class RoutingConstants {

    @Value('${spring.rabbitmq.routing.taskRun}')
    String TASK_RUN_ROUTING_KEY

    @Value('${spring.rabbitmq.routing.runHooks}')
    String RUN_HOOK_ROUTING_KEY
}
