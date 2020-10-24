package scraper.service.controller

import com.github.fge.jsonpatch.JsonPatch
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import scraper.service.amqp.dto.ExecuteCommandDto
import scraper.service.dto.model.task.PendingTaskDto
import scraper.service.dto.model.task.TaskDto
import scraper.service.dto.model.task.TaskEditDto
import scraper.service.dto.model.task.command.TaskCommandExecuteDto
import scraper.service.model.Task
import scraper.service.service.*
import scraper.service.store.FileDataRepository
import scraper.service.ws.TaskWebsocketProducer

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

    @Autowired
    TaskWebsocketProducer taskWebsocketProducer

    /**
     * @param taskId
     * @return
     */
    @GetMapping("{taskId}")
    TaskEditDto get(@PathVariable String taskId) {
        return taskService.getTaskEditDto(taskId)
    }

    /**
     * @param taskId
     * @return
     */
    @GetMapping("/suspended/{taskId}")
    Boolean isSuspended(@PathVariable String taskId) {
        return taskService.isSuspended(taskId)
    }

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

    @PatchMapping("{id}")
    TaskDto runFromApi(@PathVariable String id,
                       @RequestBody JsonPatch jsonPatch) {
        return taskService.patch(id, jsonPatch)
    }

    /**
     * Execute command to worker
     * @param dto
     */
    @PostMapping("/execute-command/v2")
    ExecuteCommandDto executeCommand(@RequestBody ExecuteCommandDto dto) {
        taskService.executeCommand(dto)
        return dto
    }


    /**
     * Notify start command Execute
     * @param dto
     */
    @PostMapping("/notify/start-command-execute")
    void notifyStartCommandExecute(@RequestBody TaskCommandExecuteDto dto) {
        taskWebsocketProducer.startCommandExecute(dto)
    }

}
