package scraper.service.scheduler


import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class NeedRunTaskStatusScheduler extends AbstractTaskStatusScheduler {

    NeedRunTaskStatusScheduler(scraper.service.TaskService taskService) {
        this.taskService = taskService
    }

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    scraper.service.UserService userService

    @Autowired
    scraper.amqp.producer.TaskProducer taskProducer

    @Override
    Boolean checkChangeTaskStatus(scraper.model.Task task) {
        if (task.status != scraper.constants.PipelineStatuses.NEED_RUN) {
            return false
        }
        boolean workerBusy = !userService.hasFreeWorker(task.userId)
        if (workerBusy) {
            return false
        }
        changeStatus(task.id, scraper.constants.PipelineStatuses.QUEUE)
        if (task.dsl?.version) {
            logger.info('push to taskRunNode')
            taskProducer.taskRunNode(task)
        } else {
            logger.info('push to taskRun')
        }
        return true
    }
}
