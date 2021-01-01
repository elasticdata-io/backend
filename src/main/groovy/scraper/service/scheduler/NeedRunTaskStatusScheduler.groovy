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
class NeedRunTaskStatusScheduler extends AbstractTaskStatusScheduler {

    NeedRunTaskStatusScheduler(TaskService taskService) {
        this.taskService = taskService
    }

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    UserService userService

    @Autowired
    TaskProducer taskProducer

    @Override
    Boolean checkChangeTaskStatus(Task task) {
        if (task.status != PipelineStatuses.NEED_RUN) {
            return false
        }
        boolean workerBusy = !userService.hasFreeWorker(task.userId)
        if (workerBusy) {
            return false
        }
        changeStatus(task.id, PipelineStatuses.QUEUE)
        if (task.dsl?.version) {
            logger.info('push to taskRunNode')
            taskProducer.taskRunNode(task)
        } else {
            logger.info('push to taskRun')
        }
        return true
    }
}
