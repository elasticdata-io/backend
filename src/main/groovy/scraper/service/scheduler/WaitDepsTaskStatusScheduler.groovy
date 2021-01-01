package scraper.service.scheduler

import org.springframework.stereotype.Component

@Component
class WaitDepsTaskStatusScheduler extends AbstractTaskStatusScheduler {

    WaitDepsTaskStatusScheduler(scraper.service.TaskService taskService) {
        this.taskService = taskService
    }

    @Override
    Boolean checkChangeTaskStatus(scraper.model.Task task) {
        if (task.status != scraper.constants.PipelineStatuses.WAIT_DEPS) {
            return false
        }
        List<scraper.model.Task> taskDependencies = taskService.findByIds(task.taskDependencies)
        if (taskDependencies.empty) {
            return false
        }
        Boolean depsIsFinished = taskDependencies.every {t ->
            t.status == scraper.constants.PipelineStatuses.COMPLETED || t.status == scraper.constants.PipelineStatuses.ERROR || t.status == scraper.constants.PipelineStatuses.STOPPED
        }
        if (depsIsFinished) {
            changeStatus(task.id, scraper.constants.PipelineStatuses.NEED_RUN)
            return true
        }
    }
}
