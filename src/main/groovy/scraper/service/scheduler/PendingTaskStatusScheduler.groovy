package scraper.service.scheduler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.service.TaskDependenciesService

@Component
class PendingTaskStatusScheduler extends AbstractTaskStatusScheduler {

    PendingTaskStatusScheduler(scraper.service.TaskService taskService) {
        this.taskService = taskService
    }

    @Autowired
    TaskDependenciesService taskDependenciesService

    @Override
    Boolean checkChangeTaskStatus(scraper.model.Task task) {
        if (task.status != scraper.constants.PipelineStatuses.PENDING) {
            return false
        }
        Boolean needDeps = taskDependenciesService.checkNeedDependencies(task)
        String newStatus = needDeps ? scraper.constants.PipelineStatuses.NEED_DEPS : scraper.constants.PipelineStatuses.NEED_RUN
        task.startOnUtc = new Date()
        this.taskService.update(task)
        changeStatus(task.id, newStatus)
        return true
    }
}
