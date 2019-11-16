package scraper.service.schedule

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import scraper.service.model.Task
import scraper.service.service.TaskService
import scraper.service.service.scheduler.NeedRunTaskStatusScheduler

@Component
class TaskRunnerSchedule {

    private static final Logger logger = LoggerFactory.getLogger(TaskRunnerSchedule.class)

    @Autowired
    NeedRunTaskStatusScheduler needRunTaskStatusScheduler

    @Autowired
    TaskService taskService

    @Scheduled(cron='*/5 * * * * * ')
    void checkRunTask() {
        List<Task> tasks = taskService.findNeedRunTasks()
        logger.info("find ${tasks.size()} need run tasks")
        tasks.each {task->
            needRunTaskStatusScheduler.checkTaskStatus(task)
        }
    }
}
