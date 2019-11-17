package scraper.service.service

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import scraper.service.amqp.producer.TaskProducer
import scraper.service.constants.PipelineStatuses
import scraper.service.dto.mapper.TaskMapper
import scraper.service.dto.model.task.TaskDto
import scraper.service.model.Pipeline
import scraper.service.model.Task
import scraper.service.repository.TaskRepository
import scraper.service.service.scheduler.TaskStatusScheduler
import scraper.service.ws.TaskWebsocketProducer

@Service
class TaskService {
    private Logger logger = LogManager.getRootLogger()

    @Autowired
    TaskProducer taskProducer

    @Autowired
    TaskRepository taskRepository

    @Autowired
    TaskExecutorService taskExecutorService

    @Autowired
    List<TaskStatusScheduler> taskStatusSchedulers

    @Autowired
    private TaskWebsocketProducer taskWebsocketProducer

    TaskDto getTask(String id) {
        Task task = findById(id)
        if (!task) {
            new Exception("task with id:${id} not found")
        }
        return TaskMapper.toTaskDto(task)
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

    Task findLastCompletedTask(String pipelineId) {
        List<String> statuses = [PipelineStatuses.COMPLETED]
        PageRequest page = new PageRequest(0, 1)
        List<Task> tasks = taskRepository.findByPipelineIdAndStatusInOrderByStartOnUtcDesc(pipelineId, statuses, page)
        if (!tasks || tasks.empty) {
            return null
        }
        return tasks.first()
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

    Task createWithoutRun(Pipeline pipeline, Task task) {
        task.pipelineId = pipeline.id
        task.startOnUtc = new Date()
        task.userId = pipeline.user.id
        task.hookUrl = pipeline.hookUrl
        task.commands = pipeline.jsonCommands
        taskRepository.save(task)
        update(task)
        return task
    }

    Task runFromQueue(String taskId) {
        Task task = findById(taskId)
        if (!task) {
            logger.error("task with id ${taskId} not found")
            return
        }
        if (task.status != PipelineStatuses.QUEUE) {
            logger.error("task ${task.id} not runnig, current status = ${task.status}")
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
        taskExecutorService.stop(task)
    }

    void update(Task task) {
        logger.info("TaskService.update taskId ${task.id}")
        try {
            taskRepository.save(task)
            taskProducer.taskChanged(task.id)
            notifyChangeTaskToClient(task)
        } catch(OptimisticLockingFailureException e) {
            logger.error("OptimisticLockingFailureException taskId = ${task.id}")
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

}
