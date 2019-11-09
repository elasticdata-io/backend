package scraper.service.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.service.model.Pipeline
import scraper.service.model.Task

@Service
class TaskDependencyAssigner {

    @Autowired
    PipelineService pipelineService

    @Autowired
    TaskService taskService

    Boolean assignDependencies(Task task) {
        def dependencyTasks = task.dependencyTaskIds
        if (dependencyTasks && !dependencyTasks.empty) {
            return false
        }
        Pipeline pipeline = pipelineService.findById(task.pipelineId)
        def dependencyPipelines = pipeline.dependencies
        if (!dependencyPipelines || dependencyPipelines.empty) {
            return false
        }
        task.dependencyTaskIds = []
        dependencyPipelines.each {dependencyPipeline ->
            def innerPipeline = pipelineService.findById(dependencyPipeline.pipelineId)
            if (innerPipeline) {
                Task innerTask = taskService.createFromParentTask(innerPipeline, task)
                task.dependencyTaskIds.add(innerTask.id)
            }
        }
        return true
    }
}
