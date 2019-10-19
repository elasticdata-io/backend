package scraper.service.amqp

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class QueueConstants {

    @Value('${spring.rabbitmq.queue.pipelineRun}')
    String PIPELINE_RUN

    @Value('${spring.rabbitmq.queue.pipelineRunHierarchy}')
    String PIPELINE_RUN_HIERARCHY

    @Value('${spring.rabbitmq.queue.pipelineStop}')
    String PIPELINE_STOP

    @Value('${spring.rabbitmq.queue.pipelineRunHooks}')
    String PIPELINE_RUN_HOOKS

    @Value('${spring.rabbitmq.queue.pipelineFinished}')
    String PIPELINE_FINISHED
}
