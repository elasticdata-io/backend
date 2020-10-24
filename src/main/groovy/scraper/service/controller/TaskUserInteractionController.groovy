package scraper.service.controller

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import scraper.service.dto.model.task.UserInteractionStateDto
import scraper.service.dto.model.task.UserInteractionDto
import scraper.service.service.TaskUserInteractionService
import scraper.service.ws.TaskWebsocketProducer

@RestController
@RequestMapping("/task-user-interaction")
class TaskUserInteractionController {

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    TaskWebsocketProducer taskWebsocketProducer

    @Autowired
    TaskUserInteractionService taskUserInteractionService

    /**
     * Change user interaction mode for task id.
     */
    @PostMapping()
    void change(@RequestBody UserInteractionStateDto dto) {
        println dto.taskId
        taskUserInteractionService.createOrUpdate(dto)
    }

    /**
     * Fetch all user interactions with task id.
     * @param taskId
     */
    @GetMapping('/{taskId}')
    List<UserInteractionDto> list(@PathVariable String taskId) {
        return taskUserInteractionService.list(taskId)
    }

}