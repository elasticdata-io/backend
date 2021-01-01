package scraper.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.model.Task

@Service
class TaskDependenciesService {

    @Autowired
    PipelineRunnerService pipelineRunnerService

    @Autowired
    TaskService taskService

    @Autowired
    PipelineService pipelineService

    Boolean checkNeedDependencies(Task task) {
        def taskDependencies = task.taskDependencies
        if (taskDependencies && !taskDependencies.empty) {
            return false
        }
        if (task.withoutDependencies) {
            return false
        }
        def pipeline = pipelineService.findById(task.pipelineId)
        def dependencies = pipeline.dependencies
        return dependencies && dependencies.empty == false
    }

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
                task.taskDependencies.add(innerTask.id)
            }
            return innerTasks
        }
        return []
    }
}
