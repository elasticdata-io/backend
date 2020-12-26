package scraper.service.service

import com.github.fge.jsonpatch.JsonPatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import scraper.service.amqp.dto.ExecuteCommandDto
import scraper.service.amqp.producer.HookProducer
import scraper.service.amqp.producer.TaskProducer
import scraper.service.constants.PipelineStatuses
import scraper.service.dto.mapper.TaskMapper
import scraper.service.dto.model.task.TaskCompleteDto
import scraper.service.dto.model.task.TaskDto
import scraper.service.dto.model.task.TaskEditDto
import scraper.service.dto.model.task.TaskErrorDto
import scraper.service.model.Pipeline
import scraper.service.model.Task
import scraper.service.model.mapper.PipelineDslMapper
import scraper.service.repository.TaskRepository
import scraper.service.service.scheduler.TaskStatusScheduler
import scraper.service.ws.TaskWebsocketProducer

@Service
class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class)

    @Autowired
    TaskProducer taskProducer

    @Autowired
    HookProducer hookProducer

    @Autowired
    TaskRepository taskRepository

    @Autowired
    List<TaskStatusScheduler> taskStatusSchedulers

    @Autowired
    private TaskWebsocketProducer taskWebsocketProducer

    @Autowired
    private PatchService patchService

    TaskDto getTask(String id) {
        Task task = findById(id)
        if (!task) {
            throw new Exception("task with id:${id} not found")
        }
        return TaskMapper.toTaskDto(task)
    }

    TaskEditDto getTaskEditDto(String id) {
        Task task = findById(id)
        if (!task) {
            throw new Exception("task with id:${id} not found")
        }
        return TaskMapper.toTaskEditDto(task)
    }

    Boolean isSuspended(String id) {
        Task task = findById(id)
        if (!task) {
            throw new Exception("task with id:${id} not found")
        }
        return PipelineStatuses.isTaskSuspended(task.status)
    }

    TaskDto patch(String id, JsonPatch patch) {
        Task task = findById(id)
        if (!task) {
            throw new Exception("task with id:${id} not found")
        }
        Task patchedTask = patchService.patch(patch, task, Task.class)
        update(patchedTask)
        return TaskMapper.toTaskDto(patchedTask)
    }

    Task findById(String id) {
        Optional<Task> task = taskRepository.findById(id)
        return task.present ? task.get() : null
    }

    List<Task> findByIds(List<String> ids) {
        return taskRepository.findByIdIn(ids)
    }

    List<Task> findByPipelineAndUserOrderByStartOnDesc(String pipelineId, String userId, Pageable top) {
        return taskRepository.findByPipelineIdAndUserIdOrderByStartOnUtcDesc(pipelineId, userId, top)
    }

    List<Task> findByPipelineAndErrorOrderByStartOnDesc(String pipelineId, String error, Pageable top) {
        return taskRepository.findByPipelineIdAndFailureReasonOrderByStartOnUtcDesc(pipelineId, error, top)
    }

    Task findLastTask(String pipelineId, List<String> statuses) {
        PageRequest page = PageRequest.of(0, 1)
        List<Task> tasks = taskRepository.findByPipelineIdAndStatusInOrderByStartOnUtcDesc(pipelineId, statuses, page)
        if (!tasks || tasks.empty) {
            return null
        }
        return tasks.first()
    }

    Task findLastCompletedTask(String pipelineId) {
        return findLastTask(pipelineId, [PipelineStatuses.COMPLETED])
    }

    Task findLastFinishedTask(String pipelineId) {
        List<String> statuses = [PipelineStatuses.COMPLETED, PipelineStatuses.ERROR, PipelineStatuses.STOPPED]
        return findLastTask(pipelineId, statuses)
    }

    List<Task> findByStatusInAndUserId(List<String> statuses, String userId) {
        return taskRepository.findByStatusInAndUserId(statuses, userId)
    }

    List<Task> findNeedRunTasks() {
        return taskRepository
                .findByStatusOrderByStartOnUtcAsc(PipelineStatuses.NEED_RUN, PageRequest.of(0, 50))
    }

    List<Task> findNeedStopTasks() {
        return taskRepository
                .findByStatusOrderByStartOnUtcAsc(PipelineStatuses.STOPPING, PageRequest.of(0, 50))
    }

    List<Task> findWaitDepsTasks() {
        return taskRepository
                .findByStatusOrderByStartOnUtcAsc(PipelineStatuses.WAIT_DEPS, PageRequest.of(0, 50))
    }

    List<Task> findWaitingOtherPipelineTasks(Pageable page) {
        return taskRepository.findByStatusInOrderByStartOnUtcAsc(
                [PipelineStatuses.WAIT_DEPS], page)
    }

    void deleteById(String id) {
        taskRepository.deleteById(id)
    }

    Task createAndRun(Pipeline pipeline) {
        Task task = createWithoutRun(pipeline, new Task())
        return updateStatus(task.id, PipelineStatuses.PENDING)
    }

    Task createAndRunWithoutDependencies(Pipeline pipeline) {
        Task task = createWithoutRun(pipeline, new Task())
        task.withoutDependencies = true
        taskRepository.save(task)
        return updateStatus(task.id, PipelineStatuses.PENDING)
    }

    Task createWithoutRun(Pipeline pipeline, Task task) {
        task.pipelineId = pipeline.id
        task.userId = pipeline.user.id
        task.hookUrl = pipeline.hookUrl
        task.dsl = pipeline.dsl ?: PipelineDslMapper.toPipelineDsl(pipeline.jsonCommands)
        taskRepository.save(task)
        update(task)
        return task
    }

    Task complete(TaskCompleteDto taskDto) {
        Task task = findById(taskDto.id)
        task.docsUrl = taskDto.docsUrl
        task.docsCount = taskDto.docsCount
        task.docsBytes = taskDto.docsBytes
        task.commandsInformationLink = taskDto.commandsInformationLink
        task.endOnUtc = new Date()
        update(task)
        updateStatus(taskDto.id, PipelineStatuses.COMPLETED)
        hookProducer.runHook(taskDto.id)
        return task
    }

    Task error(TaskErrorDto taskDto) {
        Task task = findById(taskDto.id)
        task.docsUrl = taskDto.docsUrl
        task.docsCount = taskDto.docsCount
        task.docsBytes = taskDto.docsBytes
        task.commandsInformationLink = taskDto.commandsInformationLink
        task.endOnUtc = new Date()
        task.failureReason = taskDto.failureReason
        update(task)
        updateStatus(taskDto.id, PipelineStatuses.ERROR)
        hookProducer.runHook(taskDto.id)
        return task
    }

    void update(Task task) {
        logger.info("TaskService.update taskId ${task.id}")
        try {
            taskRepository.save(task)
            taskProducer.taskChanged(task.id)
            notifyChangeTaskToClient(task)
        } catch(OptimisticLockingFailureException e) {
            logger.error(e.toString())
        }
    }

    Task updateStatus(String taskId, String status) {
        // todo: update status with only status field
        logger.info("TaskService.updateStatus taskId ${taskId}, status = ${status}")
        Task task = findById(taskId)
        task.status = status
        taskRepository.save(task)
        taskStatusSchedulers.each {scheduler ->
            scheduler.checkChangeTaskStatus(task)
        }
        notifyChangeTaskToClient(task)
        return task
    }

    private void notifyChangeTaskToClient(Task task) {
        def taskDto = TaskMapper.toTaskDto(task)
        taskWebsocketProducer.change(taskDto)
    }

    void executeCommand(ExecuteCommandDto executeCommandDto) {
        taskProducer.executeCommand(executeCommandDto)
    }
}
