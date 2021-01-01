package scraper.service.scheduler

import org.springframework.stereotype.Component
import scraper.constants.PipelineStatuses
import scraper.model.Task
import scraper.service.TaskService

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
            t.status == PipelineStatuses.COMPLETED || t.status == PipelineStatuses.ERROR || t.status == scraper.constants.PipelineStatuses.STOPPED
        }
        if (depsIsFinished) {
            changeStatus(task.id, PipelineStatuses.NEED_RUN)
            return true
        }
    }
}
