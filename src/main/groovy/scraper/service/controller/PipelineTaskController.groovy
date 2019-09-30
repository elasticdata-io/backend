package scraper.service.controller

import org.springframework.amqp.core.AmqpTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import scraper.service.model.Pipeline
import scraper.service.model.PipelineTask
import scraper.service.repository.PipelineRepository
import scraper.service.repository.PipelineTaskRepository
import scraper.service.auth.TokenService
import scraper.service.service.PipelineTaskService

@RestController
@RequestMapping("/pipeline-task")
class PipelineTaskController {

    @Autowired
    PipelineRepository pipelineRepository

    @Autowired
    PipelineTaskService pipelineTaskService

    @Autowired
    PipelineTaskRepository pipelineTaskRepository

    @Autowired
    TokenService tokenService

    @Autowired
    AmqpTemplate rabbitTemplate

    @Autowired
    private SimpMessagingTemplate messagingTemplate

    @RequestMapping("/list/{pipelineId}")
    List<PipelineTask> list(@PathVariable String pipelineId, @RequestHeader("token") String token) {
        String userId = tokenService.getUserId(token)
        Pipeline pipeline = pipelineRepository.findByIdAndUser(pipelineId, userId)
        if (!pipeline) {
            return null
        }
        Pageable top = new PageRequest(0, 10)
        List<PipelineTask> tasks = pipelineTaskRepository.findByPipelineOrderByStartOnDesc(pipelineId, top)
        tasks.each {task->
            String error = task.error
            task.error = error?.take(1000)
        }
        return tasks
    }

    @RequestMapping("/delete/{id}")
    void delete(@PathVariable String id, @RequestHeader("token") String token) {
        if (checkPermission(id, token)) {
            pipelineTaskRepository.delete(id)
        }
    }

    @RequestMapping("/data/{id}")
    List<HashMap> getData(@PathVariable String id) {
        PipelineTask pipelineTask = pipelineTaskService.findById(id)
        return pipelineTask.data
    }

    private boolean checkPermission(String taskId, String token) {
        PipelineTask task = pipelineTaskService.findById(taskId)
        String userId = tokenService.getUserId(token)
        Pipeline pipeline = pipelineRepository.findByIdAndUser(task.pipeline.id, userId)
        return pipeline != null
    }
}
