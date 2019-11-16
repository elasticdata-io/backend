package scraper.service.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.service.model.Task
import scraper.service.model.TaskDependency

@Service
class TaskDependenciesScheduler {

    @Autowired
    PipelineRunnerService pipelineRunnerService

    @Autowired
    TaskService taskService

    @Autowired
    PipelineService pipelineService

    List<Task> createTaskDependencies(Task task) {
        def taskDependencies = task.taskDependencies
        if (taskDependencies && !taskDependencies.empty) {
            return []
        }
        def pipeline = pipelineService.findById(task.pipelineId)
        def dependencies = pipeline.dependencies
        if (dependencies && !dependencies.empty) {
            task.taskDependencies = []
            def innerTasks = []
            dependencies.each {dep ->
                def innerTask = pipelineRunnerService.pendingFromDependencies(dep.pipelineId, task.id)
                innerTasks.add(innerTask)
                def dependency = new TaskDependency(
                        dependencyTaskId: innerTask.id,
                        dependencyTaskStatus: innerTask.status
                )
                task.taskDependencies.add(dependency)
            }
            return innerTasks
        }
        return []
    }

    Task updateParentTask(Task task) {
        def parentTaskId = task.parentTaskId
        if (!parentTaskId) {
            return
        }
        def parentTask = taskService.findById(parentTaskId)
        def dependencyTask = parentTask.taskDependencies.find {dep ->
            return dep.dependencyTaskId == task.id
        }
        dependencyTask.dependencyTaskStatus = task.status
        return parentTask
    }
}
