package scraper.service.schedule

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import scraper.service.model.Task
import scraper.service.service.TaskService
import scraper.service.service.scheduler.NeedRunTaskStatusScheduler
import scraper.service.service.scheduler.StoppingTaskStatusScheduler
import scraper.service.service.scheduler.WaitDepsTaskStatusScheduler

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
        List<Task> tasks = taskService.findNeedStopTasks()
        if (tasks.size()) {
            // logger.info("find ${tasks.size()} need stop tasks")
        }
        tasks.each {task->
            stoppingTaskStatusScheduler.checkChangeTaskStatus(task)
        }
    }

    @Scheduled(cron='*/5 * * * * * ')
    void checkWaitDepsTask() {
        List<Task> tasks = taskService.findWaitDepsTasks()
        if (tasks.size()) {
            // logger.info("find ${tasks.size()} wait deps tasks")
        }
        tasks.each {task->
            waitDepsTaskStatusScheduler.checkChangeTaskStatus(task)
        }
    }
}
