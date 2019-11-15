package scraper.service.amqp.consumer

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.service.amqp.QueueConstants
import scraper.service.service.TaskService
import scraper.service.service.TaskStatusControllerManager

@Component
class TaskWatchConsumer {

    @Autowired
    TaskService taskService

    @Autowired
    QueueConstants queueConstants

    @Autowired
    TaskStatusControllerManager taskStatusControllerManager

    /**
     * @param taskId
     */
    @RabbitListener(queues = '#{queueConstants.TASK_CHANGED}', containerFactory="defaultConnectionFactory")
    void watchTaskChangedWorker(String taskId) {
        def task = taskService.findById(taskId)
        taskStatusControllerManager.update(task)
    }
}
