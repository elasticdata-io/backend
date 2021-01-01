package scraper.controller

import org.springframework.amqp.core.AmqpTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import scraper.dto.mapper.TaskMapper
import scraper.dto.model.task.TaskDto
import scraper.repository.PipelineRepository
import scraper.auth.TokenService

@RestController
@RequestMapping("/pipeline-task")
class PipelineTaskController {

    @Autowired
    PipelineRepository pipelineRepository

    @Autowired
    scraper.service.TaskService taskService

    @Autowired
    TokenService tokenService

    @Autowired
    AmqpTemplate rabbitTemplate

    @Autowired
    private SimpMessagingTemplate messagingTemplate

    @GetMapping("/list/{pipelineId}/{offset}/{count}")
    List<TaskDto> list(@PathVariable String pipelineId, @PathVariable Number offset, @PathVariable Number count,
                       @RequestHeader("token") String token) {
        String userId = tokenService.getUserId(token)
        Pageable top = PageRequest.of(offset as int, count as int)
        List<scraper.model.Task> tasks = taskService.findByPipelineAndUserOrderByStartOnDesc(pipelineId, userId, top)
        ArrayList<TaskDto> dtoList = new ArrayList<>()
        tasks.each {task->
            String error = task.failureReason
            task.failureReason = error?.take(1000)
            dtoList.add(TaskMapper.toTaskDto(task))
        }
        return dtoList
    }

}
