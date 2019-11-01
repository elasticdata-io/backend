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
import scraper.service.dto.mapper.TaskMapper
import scraper.service.dto.model.task.TaskDto
import scraper.service.model.Pipeline
import scraper.service.model.Task
import scraper.service.repository.PipelineRepository
import scraper.service.auth.TokenService
import scraper.service.service.TaskService
import scraper.service.store.FileDataRepository

@RestController
@RequestMapping("/pipeline-task")
class PipelineTaskController {

    @Autowired
    PipelineRepository pipelineRepository

    @Autowired
    FileDataRepository fileDataRepository

    @Autowired
    TaskService taskService

    @Autowired
    TokenService tokenService

    @Autowired
    AmqpTemplate rabbitTemplate

    @Autowired
    private SimpMessagingTemplate messagingTemplate

    @RequestMapping("/list/{pipelineId}/{offset}/{count}")
    List<TaskDto> list(@PathVariable String pipelineId, @PathVariable Number offset, @PathVariable Number count,
                       @RequestHeader("token") String token) {
        String userId = tokenService.getUserId(token)
        Pageable top = new PageRequest(offset as int, count as int)
        List<Task> tasks = taskService.findByPipelineAndUserOrderByStartOnDesc(pipelineId, userId, top)
        ArrayList<TaskDto> dtoList = new ArrayList<>()
        tasks.each {task->
            String error = task.failureReason
            task.failureReason = error?.take(1000)
            dtoList.add(TaskMapper.toTaskDto(task))
        }
        return dtoList
    }

    @RequestMapping("/delete/{id}")
    void delete(@PathVariable String id, @RequestHeader("token") String token) {
        if (checkPermission(id, token)) {
            taskService.deleteById(id)
        }
    }

    /**
     * @deprecated see TaskController->getData
     * @param id
     * @return
     */
    @RequestMapping("/data/{id}")
    List<HashMap> getData(@PathVariable String id) {
        Task task = taskService.findById(id)
        return fileDataRepository.getDataFileToList(task)
    }

    private boolean checkPermission(String taskId, String token) {
        Task task = taskService.findById(taskId)
        String userId = tokenService.getUserId(token)
        Pipeline pipeline = pipelineRepository.findByIdAndUser(task.pipelineId, userId)
        return pipeline != null
    }
}
