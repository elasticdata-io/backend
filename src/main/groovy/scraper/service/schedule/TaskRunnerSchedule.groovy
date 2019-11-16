package scraper.service.schedule

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import scraper.service.model.Task
import scraper.service.service.TaskService
import scraper.service.service.TaskStatusControllerManager

@Component
class TaskRunnerSchedule {

    private static final Logger logger = LoggerFactory.getLogger(TaskRunnerSchedule.class)

    @Autowired
    AmqpTemplate rabbitTemplate

    @Autowired
    TaskService taskService

    @Autowired
    TaskStatusControllerManager taskStatusControllerManager

    @Scheduled(cron='*/5 * * * * * ')
    void checkRunTask() {
        Pageable page = PageRequest.of(0, 20)
        List<Task> tasks = taskService.findWaitingOtherPipelineTasks(page)
        tasks.each {task->
            taskStatusControllerManager.handleWaitOtherPipelineTask(task)
        }
    }
}
