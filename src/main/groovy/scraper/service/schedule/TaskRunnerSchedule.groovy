package scraper.service.schedule

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import scraper.service.model.Task
import scraper.service.service.TaskService
import scraper.service.service.scheduler.NeedRunTaskStatusScheduler
import scraper.service.service.scheduler.StoppingTaskStatusScheduler

@Component
class TaskRunnerSchedule {

    private static final Logger logger = LoggerFactory.getLogger(TaskRunnerSchedule.class)

    @Autowired
    NeedRunTaskStatusScheduler needRunTaskStatusScheduler

    @Autowired
    StoppingTaskStatusScheduler stoppingTaskStatusScheduler

    @Autowired
    TaskService taskService

    @Scheduled(cron='*/5 * * * * * ')
    void checkRunTask() {
        List<Task> tasks = taskService.findNeedRunTasks()
        logger.info("find ${tasks.size()} need run tasks")
        tasks.each {task->
            needRunTaskStatusScheduler.checkChangeTaskStatus(task)
        }
    }

    @Scheduled(cron='*/5 * * * * * ')
    void checkStopTask() {
        List<Task> tasks = taskService.findNeedStopTasks()
        logger.info("find ${tasks.size()} need stop tasks")
        tasks.each {task->
            stoppingTaskStatusScheduler.checkChangeTaskStatus(task)
        }
    }
}
