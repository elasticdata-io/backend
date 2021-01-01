package scraper.controller

import com.github.fge.jsonpatch.JsonPatch
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import scraper.service.PipelineRunnerService
import scraper.service.PipelineService
import scraper.service.TaskService
import scraper.amqp.dto.ExecuteCommandDto
import scraper.dto.model.task.TaskCompleteDto
import scraper.dto.model.task.PendingTaskDto
import scraper.dto.model.task.TaskDto
import scraper.dto.model.task.TaskEditDto
import scraper.dto.model.task.TaskErrorDto
import scraper.dto.model.task.command.TaskCommandExecuteDto
import scraper.ws.TaskWebsocketProducer

@RestController
@RequestMapping("/task")
class TaskController {

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    PipelineRunnerService pipelineRunnerService

    @Autowired
    TaskService taskService

    @Autowired
    PipelineService pipelineService

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

    @PatchMapping("{id}")
    TaskDto runFromApi(@PathVariable String id,
                       @RequestBody JsonPatch jsonPatch) {
        return taskService.patch(id, jsonPatch)
    }

    /**
     * Completed task by id.
     * Start after completed jobs.
     * @param taskId
     */
    @PostMapping("/complete")
    void complete(@RequestBody TaskCompleteDto taskDto) {
        def task = taskService.complete(taskDto)
        pipelineService.updateFromTask(task)
    }

    /**
     * Error task by id.
     * Start after completed jobs.
     * @param taskId
     */
    @PostMapping("/error")
    void complete(@RequestBody TaskErrorDto taskDto) {
        def task = taskService.error(taskDto)
        pipelineService.updateFromTask(task)
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
