package scraper.service.service.scheduler

import groovy.json.JsonSlurper
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
        def jsonSlurper = new JsonSlurper()
        HashMap map = jsonSlurper.parseText(task.commands) as HashMap<String, String>
        changeStatus(task.id, PipelineStatuses.QUEUE)
        if (map.containsKey('version')) {
            logger.info('push to taskRunNode')
            taskProducer.taskRunNode(task.id)
        } else {
            logger.info('push to taskRun')
            taskProducer.taskRun(task.id)
        }
        return true
    }
}
