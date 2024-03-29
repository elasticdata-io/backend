package scraper.service.scheduler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.constants.PipelineStatuses
import scraper.model.Task
import scraper.service.TaskDependenciesService
import scraper.service.TaskService

@Component
class NeedDepsTaskStatusScheduler extends AbstractTaskStatusScheduler {

    NeedDepsTaskStatusScheduler(TaskService taskService) {
        this.taskService = taskService
    }

    @Autowired
    TaskDependenciesService taskDependenciesService

    @Override
    Boolean checkChangeTaskStatus(Task task) {
        if (task.status != PipelineStatuses.NEED_DEPS) {
            return false
        }
        changeStatus(task.id, PipelineStatuses.WAIT_DEPS)
        task = taskService.findById(task.id)
        List<Task> innerTasks = taskDependenciesService.createTaskDependencies(task)
        taskService.update(task)
        innerTasks.each {innerTask ->
            changeStatus(innerTask.id, PipelineStatuses.PENDING)
        }
        return true
    }
}
