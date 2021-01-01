package scraper.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import scraper.dto.model.task.UserInteractionStateDto
import scraper.dto.model.task.UserInteractionDto
import scraper.service.TaskUserInteractionService
import scraper.ws.TaskWebsocketProducer

@RestController
@RequestMapping("/task-user-interaction")
class TaskUserInteractionController {

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

    /**
     * Disable interaction mode.
     * @param interactionId
     */
    @PostMapping('/disable/{interactionId}')
    void disable(@PathVariable String interactionId) {
        taskUserInteractionService.disable(interactionId)
    }

}
