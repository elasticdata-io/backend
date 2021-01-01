package scraper.service.scheduler

import org.springframework.stereotype.Component
import scraper.constants.PipelineStatuses
import scraper.model.Task
import scraper.service.TaskService

@Component
class StoppedTaskStatusScheduler extends AbstractTaskStatusScheduler {

    StoppedTaskStatusScheduler(TaskService taskService) {
        this.taskService = taskService
    }

    @Override
    Boolean checkChangeTaskStatus(Task task) {
        if (task.status != PipelineStatuses.STOPPED) {
            return false
        }
        task.endOnUtc = new Date()
        taskService.update(task)
        return true
    }
}
