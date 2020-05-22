package scraper.service.amqp.consumer

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.service.amqp.QueueConstants
import scraper.service.service.TaskService

@Component
class TaskRunnerConsumer {

    @Autowired
    TaskService taskService

    @Autowired
    QueueConstants queueConstants

    /**
     * @param taskId
     */
    @RabbitListener(queues = '#{queueConstants.PIPELINE_TASK_RUN}', containerFactory="defaultConnectionFactory")
    void runPipelineTaskWorker(String taskId) {
        taskService.runFromQueue(taskId)
    }

    /**
     * @param taskId
     */
    @RabbitListener(queues = '#{queueConstants.PIPELINE_TASK_STOP_V1}', containerFactory="defaultConnectionFactory")
    void stopPipelineTaskWorker(String taskId) {
        taskService.stopFromQueue(taskId)
    }

    /**
     * @param taskId
     */
    @RabbitListener(queues = '#{queueConstants.PIPELINE_TASK_FINISHED}', containerFactory="defaultConnectionFactory")
    void finishedPipelineTaskWorker(String taskId) {
        // taskQueueService.toFinishedTaskQueue(taskId)
    }
}
