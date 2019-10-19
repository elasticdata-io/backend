package scraper.service.amqp

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class RoutingConstants {

    @Value('${spring.rabbitmq.routing.pipelineTaskFinish}')
    String PIPELINE_TASK_FINISH
}
