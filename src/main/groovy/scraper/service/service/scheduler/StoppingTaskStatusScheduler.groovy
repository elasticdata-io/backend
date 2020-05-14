package scraper.service.service.scheduler

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.service.amqp.producer.TaskProducer
import scraper.service.constants.PipelineStatuses
import scraper.service.model.Task
import scraper.service.service.TaskService
import scraper.service.service.UserService

@Component
class StoppingTaskStatusScheduler extends AbstractTaskStatusScheduler {

    StoppingTaskStatusScheduler(TaskService taskService) {
        this.taskService = taskService
    }

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    UserService userService

    @Autowired
    TaskProducer taskProducer

    @Override
    Boolean checkChangeTaskStatus(Task task) {
        if (task.status != PipelineStatuses.STOPPING) {
            return false
        }
        if (!task.pipelineVersion) {
            taskProducer.taskStopV1(task.id)
            return
        }
        taskProducer.taskStopV2(task.id)
        return true
    }
}
