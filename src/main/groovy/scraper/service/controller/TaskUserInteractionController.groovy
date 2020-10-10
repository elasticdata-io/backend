package scraper.service.controller

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import scraper.service.dto.model.task.EnableUserInteractionStateDto
import scraper.service.service.TaskService
import scraper.service.ws.TaskWebsocketProducer

@RestController
@RequestMapping("/task-user-interaction")
class TaskUserInteractionController {

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    TaskService taskService

    @Autowired
    TaskWebsocketProducer taskWebsocketProducer

    /**
     * Stop task by id.
     * @param taskId
     */
    @PostMapping("/enable")
    void enable(@RequestBody EnableUserInteractionStateDto dto) {
        println dto.currentUrl
        println dto.pageContext
    }

}
