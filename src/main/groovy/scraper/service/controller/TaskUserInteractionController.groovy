package scraper.service.controller

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import scraper.service.dto.model.task.EnableUserInteractionModeDto
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
     * Enable user interaction mode for task id.
     * @param taskId
     */
    @PostMapping("/enable")
    void enable(@RequestBody EnableUserInteractionModeDto dto) {
        taskUserInteractionService.createOrUpdate(dto)
    }

}
