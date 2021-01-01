package scraper.service.scheduler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.service.TaskDependenciesService

@Component
class NeedDepsTaskStatusScheduler extends AbstractTaskStatusScheduler {

    NeedDepsTaskStatusScheduler(scraper.service.TaskService taskService) {
        this.taskService = taskService
    }

    @Autowired
    TaskDependenciesService taskDependenciesService

    @Override
    Boolean checkChangeTaskStatus(scraper.model.Task task) {
        if (task.status != scraper.constants.PipelineStatuses.NEED_DEPS) {
            return false
        }
        changeStatus(task.id, scraper.constants.PipelineStatuses.WAIT_DEPS)
        task = taskService.findById(task.id)
        List<scraper.model.Task> innerTasks = taskDependenciesService.createTaskDependencies(task)
        taskService.update(task)
        innerTasks.each {innerTask ->
            changeStatus(innerTask.id, scraper.constants.PipelineStatuses.PENDING)
        }
        return true
    }
}
