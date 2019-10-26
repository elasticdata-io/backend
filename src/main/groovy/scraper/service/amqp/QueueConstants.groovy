package scraper.service.amqp

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class QueueConstants {

    @Value('${spring.rabbitmq.queue.pipelineTaskRun}')
    String PIPELINE_TASK_RUN

    @Value('${spring.rabbitmq.queue.pipelineTaskStop}')
    String PIPELINE_TASK_STOP

    @Value('${spring.rabbitmq.queue.pipelineRunHooks}')
    String PIPELINE_RUN_HOOKS

    @Value('${spring.rabbitmq.queue.pipelineFinished}')
    String PIPELINE_FINISHED
}
