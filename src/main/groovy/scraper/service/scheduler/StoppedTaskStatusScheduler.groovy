package scraper.service.scheduler

import org.springframework.stereotype.Component

@Component
class StoppedTaskStatusScheduler extends AbstractTaskStatusScheduler {

    StoppedTaskStatusScheduler(scraper.service.TaskService taskService) {
        this.taskService = taskService
    }

    @Override
    Boolean checkChangeTaskStatus(scraper.model.Task task) {
        if (task.status != scraper.constants.PipelineStatuses.STOPPED) {
            return false
        }
        task.endOnUtc = new Date()
        taskService.update(task)
        return true
    }
}
