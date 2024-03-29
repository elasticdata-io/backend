package scraper.service.scheduler

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.amqp.producer.TaskProducer
import scraper.constants.PipelineStatuses
import scraper.model.Task
import scraper.service.TaskService
import scraper.service.UserService

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
        taskProducer.taskStopV2(task.id)
        return true
    }
}
