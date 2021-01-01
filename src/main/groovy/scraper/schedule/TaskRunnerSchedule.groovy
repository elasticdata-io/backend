package scraper.schedule

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import scraper.model.Task
import scraper.service.TaskService
import scraper.service.scheduler.NeedRunTaskStatusScheduler
import scraper.service.scheduler.StoppingTaskStatusScheduler
import scraper.service.scheduler.WaitDepsTaskStatusScheduler

@Component
class TaskRunnerSchedule {

    @Autowired
    NeedRunTaskStatusScheduler needRunTaskStatusScheduler

    @Autowired
    WaitDepsTaskStatusScheduler waitDepsTaskStatusScheduler

    @Autowired
    StoppingTaskStatusScheduler stoppingTaskStatusScheduler

    @Autowired
    TaskService taskService

    @Scheduled(cron='*/5 * * * * * ')
    void checkRunTask() {
        List<Task> tasks = taskService.findNeedRunTasks()
        if (tasks.size()) {
            // logger.info("find ${tasks.size()} need run tasks")
        }
        tasks.each {task->
            needRunTaskStatusScheduler.checkChangeTaskStatus(task)
        }
    }

    @Scheduled(cron='*/5 * * * * * ')
    void checkStopTask() {
        List<scraper.model.Task> tasks = taskService.findNeedStopTasks()
        if (tasks.size()) {
            // logger.info("find ${tasks.size()} need stop tasks")
        }
        tasks.each {task->
            stoppingTaskStatusScheduler.checkChangeTaskStatus(task)
        }
    }

    @Scheduled(cron='*/5 * * * * * ')
    void checkWaitDepsTask() {
        List<scraper.model.Task> tasks = taskService.findWaitDepsTasks()
        if (tasks.size()) {
            // logger.info("find ${tasks.size()} wait deps tasks")
        }
        tasks.each {task->
            waitDepsTaskStatusScheduler.checkChangeTaskStatus(task)
        }
    }
}
