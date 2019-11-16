package scraper.service.amqp.consumer

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.service.amqp.QueueConstants
import scraper.service.service.TaskService

@Component
class TaskWatchConsumer {
    private Logger logger = LogManager.getRootLogger()

    @Autowired
    TaskService taskService

    @Autowired
    QueueConstants queueConstants

    /**
     * @param taskId
     */
    @RabbitListener(queues = '#{queueConstants.TASK_CHANGED}', containerFactory="defaultConnectionFactory")
    void watchTaskChangedWorker(String taskId) {
        // logger.info("watchTaskChangedWorker taskId: ${taskId}")
        // def task = taskService.findById(taskId)
        // taskStatusControllerManager.update(task)
    }
}
