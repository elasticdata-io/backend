package scraper.service.scheduler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.constants.PipelineStatuses
import scraper.model.Task
import scraper.service.TaskDependenciesService
import scraper.service.TaskService
import scraper.service.workermanager.WorkersScaler

@Component
class PendingTaskStatusScheduler extends AbstractTaskStatusScheduler {

    PendingTaskStatusScheduler(TaskService taskService) {
        this.taskService = taskService
    }

    @Autowired
    WorkersScaler workersScaler

    @Autowired
    TaskDependenciesService taskDependenciesService

    @Override
    Boolean checkChangeTaskStatus(Task task) {
        if (task.status != PipelineStatuses.PENDING) {
            return false
        }
        Boolean needDeps = taskDependenciesService.checkNeedDependencies(task)
        String newStatus = needDeps ? PipelineStatuses.NEED_DEPS : PipelineStatuses.NEED_RUN
        task.startOnUtc = new Date()
        this.taskService.update(task)
        changeStatus(task.id, newStatus)
        workersScaler.checkReplicasUp(task.userId)
        return true
    }
}
