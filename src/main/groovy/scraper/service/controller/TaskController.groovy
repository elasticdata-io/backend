package scraper.service.controller

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import scraper.service.dto.model.task.PendingTaskDto
import scraper.service.dto.model.task.TaskDto
import scraper.service.model.Task
import scraper.service.service.*
import scraper.service.store.FileDataRepository

@RestController
@RequestMapping("/task")
class TaskController {

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    PipelineRunnerService pipelineRunnerService

    @Autowired
    FileDataRepository fileDataRepository

    @Autowired
    TaskService taskService

    /**
     * Stop task by id.
     * @param taskId
     */
    @PostMapping("/stop/{taskId}")
    PendingTaskDto stop(@PathVariable String taskId) {
        logger.info("request stop task ${taskId}")
        // todo: процес должен быть остановлен именно на той ноде где запущен воркер
        return pipelineRunnerService.stoppingFromClient(taskId)
    }

    /**
     * @param taskId
     * @return
     */
    @GetMapping("/status/{taskId}")
    TaskDto status(@PathVariable String taskId) {
        logger.info("request status task ${taskId}")
        return taskService.getTask(taskId)
    }

    /**
     * @param taskId
     * @return
     */
    @RequestMapping("/data/{taskId}")
    List<HashMap> getData(@PathVariable String taskId) {
        Task task = taskService.findById(taskId)
        return fileDataRepository.getDataFileToList(task)
    }

}
