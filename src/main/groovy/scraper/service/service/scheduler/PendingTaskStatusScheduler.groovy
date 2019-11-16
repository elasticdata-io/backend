package scraper.service.service.scheduler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.service.constants.PipelineStatuses
import scraper.service.model.Task
import scraper.service.service.TaskDependenciesService
import scraper.service.service.TaskService

@Component
class PendingTaskStatusScheduler extends AbstractTaskStatusScheduler {

    PendingTaskStatusScheduler(TaskService taskService) {
        this.taskService = taskService
    }

    @Autowired
    TaskDependenciesService taskDependenciesService

    @Override
    Boolean checkTaskStatus(Task task) {
        if (task.status != PipelineStatuses.PENDING) {
            return false
        }
        Boolean needDeps = taskDependenciesService.checkNeedDependencies(task)
        String newStatus = needDeps ? PipelineStatuses.NEED_DEPS : PipelineStatuses.NEED_RUN
        changeStatus(task.id, newStatus)
        return true
    }
}
