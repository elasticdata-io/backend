package scraper.service.scheduler

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class StoppingTaskStatusScheduler extends AbstractTaskStatusScheduler {

    StoppingTaskStatusScheduler(scraper.service.TaskService taskService) {
        this.taskService = taskService
    }

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    scraper.service.UserService userService

    @Autowired
    scraper.amqp.producer.TaskProducer taskProducer

    @Override
    Boolean checkChangeTaskStatus(scraper.model.Task task) {
        if (task.status != scraper.constants.PipelineStatuses.STOPPING) {
            return false
        }
        taskProducer.taskStopV2(task.id)
        return true
    }
}
