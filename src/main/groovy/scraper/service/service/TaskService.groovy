package scraper.service.service

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import scraper.service.constants.PipelineStatuses
import scraper.service.dto.mapper.TaskMapper
import scraper.service.dto.model.task.TaskDto
import scraper.service.model.Pipeline
import scraper.service.model.Task
import scraper.service.repository.TaskRepository
import scraper.service.ws.TaskWebsocketProducer

@Service
class TaskService {
    private Logger logger = LogManager.getRootLogger()

    @Autowired
    TaskStatusControllerManager taskStatusControllerManager

    @Autowired
    TaskRepository taskRepository

    @Autowired
    TaskExecutorService taskExecutorService

    @Autowired
    private TaskWebsocketProducer taskWebsocketProducer

    TaskDto getTask(String id) {
        def task = findById(id)
        return TaskMapper.toTaskDto(task)
    }

    Task findById(String id) {
        Optional<Task> task = taskRepository.findById(id)
        return task.present ? task.get() : null
    }

    List<Task> findAllById(List <String> ids) {
        return taskRepository.findAllById(ids)
    }

    List<Task> findByPipelineAndUserOrderByStartOnDesc(String pipelineId, String userId, Pageable top) {
        return taskRepository.findByPipelineIdAndUserIdOrderByStartOnUtcDesc(pipelineId, userId, top)
    }

    List<Task> findByPipelineAndErrorOrderByStartOnDesc(String pipelineId, String error, Pageable top) {
        return taskRepository.findByPipelineIdAndFailureReasonOrderByStartOnUtcDesc(pipelineId, error, top)
    }

    List<Task> findByStatusInAndUserId(List<String> statuses, String userId) {
        return taskRepository.findByStatusInAndUserId(statuses, userId)
    }

    Task findFirstWaitingTaskByUserId(String userId) {
        def tasks = taskRepository.findByStatusInAndUserIdOrderByStartOnUtcDesc([PipelineStatuses.PENDING], userId)
        if (tasks.empty) {
            return
        }
        return tasks.first()
    }

    Task findByDependencyTasksAndUserId(String dependencyTaskId, String userId) {
        return taskRepository.findByDependencyTasksAndUserId(dependencyTaskId, userId)
    }

    void deleteById(String id) {
        taskRepository.deleteById(id)
    }

    Task createFromParentTask(Pipeline pipeline, Task parentTask) {
        Task task = new Task(
                pipelineId: pipeline.id,
                startOnUtc: new Date(),
                userId: pipeline.user.id,
                hookUrl: pipeline.hookUrl,
                commands: pipeline.jsonCommands,
                status: PipelineStatuses.PENDING,
                parentTaskId: parentTask.id,
        )
        update(task)
        return task
    }

    Task createFromPipeline(Pipeline pipeline) {
        Task task = new Task(
                pipelineId: pipeline.id,
                startOnUtc: new Date(),
                userId: pipeline.user.id,
                hookUrl: pipeline.hookUrl,
                commands: pipeline.jsonCommands,
                status: PipelineStatuses.PENDING
        )
        update(task)
        return task
    }

    Task runFromQueue(String taskId) {
        Task task = findById(taskId)
        if (!task) {
            logger.error("task with id ${taskId} not found")
            return
        }
        return taskExecutorService.run(task)
    }

    Task stopFromQueue(String taskId) {
        Task task = findById(taskId)
        if (!task) {
            logger.error("task with id ${taskId} not found")
            return
        }
        return taskExecutorService.stop(task)
    }

    void update(Task task) {
        taskRepository.save(task)
        taskStatusControllerManager.update(task)
        notifyChangeTaskToClient(task)
    }

    private void notifyChangeTaskToClient(Task task) {
        def taskDto = TaskMapper.toTaskDto(task)
        taskWebsocketProducer.change(taskDto)
    }

}
