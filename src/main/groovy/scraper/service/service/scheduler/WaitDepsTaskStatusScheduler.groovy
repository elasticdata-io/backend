package scraper.service.service.scheduler

import org.springframework.stereotype.Component
import scraper.service.constants.PipelineStatuses
import scraper.service.model.Task
import scraper.service.service.TaskService

@Component
class WaitDepsTaskStatusScheduler extends AbstractTaskStatusScheduler {

    WaitDepsTaskStatusScheduler(TaskService taskService) {
        this.taskService = taskService
    }

    @Override
    Boolean checkChangeTaskStatus(Task task) {
        if (task.status != PipelineStatuses.WAIT_DEPS) {
            return false
        }
        List<Task> taskDependencies = taskService.findByIds(task.taskDependencies)
        if (taskDependencies.empty) {
            return false
        }
        Boolean depsIsFinished = taskDependencies.every {t ->
            t.status == PipelineStatuses.COMPLETED || t.status == PipelineStatuses.ERROR || t.status == PipelineStatuses.STOPPED
        }
        if (depsIsFinished) {
            changeStatus(task.id, PipelineStatuses.NEED_RUN)
            return true
        }
    }
}
