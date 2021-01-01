package scraper.controller

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import scraper.dto.model.task.PendingApiTaskDto
import scraper.dto.model.task.PendingTaskDto
import scraper.dto.model.task.PipelineRunDto
import scraper.service.PipelineService
import scraper.repository.PipelineRepository

@RestController
@RequestMapping("/pipeline")
class PipelineController {

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    scraper.service.TaskService taskService

    @Autowired
    PipelineRepository pipelineRepository

    @Autowired
    PipelineService pipelineService

    @Autowired
    scraper.service.PipelineRunnerService pipelineRunnerService

    /**
     * Runs pipeline process by pipeline pipelineId.
     * @param id
     */
    @PostMapping("/run/{id}")
    PendingApiTaskDto runFromApi(@PathVariable String id, @RequestBody(required=false) PipelineRunDto dto) {
        logger.info("request run task ${id}")
        return pipelineRunnerService.pendingFromApi(id, dto)
    }

    /**
     * Runs pipeline process by pipeline pipelineId.
     * @param id
     */
    @PostMapping("/run-from-client/{id}")
    PendingTaskDto runFromClient(@PathVariable String id) {
        return pipelineRunnerService.pendingFromClient(id)
    }

    @PostMapping("/task/synchronize/{taskId}")
    void syncWithLastTask(@PathVariable String taskId) {
        scraper.model.Task task = taskService.findById(taskId)
        scraper.model.Task lastTask = taskService.findLastFinishedTask(task.pipelineId)
        pipelineService.updateFromTask(lastTask)
    }

}
