package scraper.service.service.scheduler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.service.constants.PipelineStatuses
import scraper.service.model.Task
import scraper.service.service.TaskDependenciesService
import scraper.service.service.TaskService

@Component
class NeedDepsTaskStatusScheduler extends AbstractTaskStatusScheduler {

    NeedDepsTaskStatusScheduler(TaskService taskService) {
        this.taskService = taskService
    }

    @Autowired
    TaskDependenciesService taskDependenciesService

    @Override
    Boolean checkTaskStatus(Task task) {
        if (task.status != PipelineStatuses.NEED_DEPS) {
            return false
        }
        changeStatus(task.id, PipelineStatuses.WAIT_DEPS)
        List<Task> innerTasks = taskDependenciesService.createTaskDependencies(task)
        innerTasks.each {innerTask ->
            changeStatus(innerTask.id, PipelineStatuses.PENDING)
        }
        return true
    }
}
