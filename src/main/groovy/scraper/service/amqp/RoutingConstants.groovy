package scraper.service.amqp

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class RoutingConstants {

    @Value('${spring.rabbitmq.routing.taskChanged}')
    String TASK_CHANGED

    @Value('${spring.rabbitmq.routing.pipelineTaskRun}')
    String PIPELINE_TASK_RUN

    @Value('${spring.rabbitmq.routing.pipelineTaskStop}')
    String PIPELINE_TASK_STOP

    @Value('${spring.rabbitmq.routing.pipelineTaskFinish}')
    String PIPELINE_TASK_FINISH

    @Value('${spring.rabbitmq.routing.pipelineFinish}')
    String PIPELINE_FINISH
}
