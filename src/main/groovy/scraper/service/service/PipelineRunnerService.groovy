package scraper.service.service

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.service.constants.PipelineStatuses
import scraper.service.dto.mapper.TaskMapper
import scraper.service.dto.model.task.PendingApiTaskDto
import scraper.service.dto.model.task.PendingTaskDto
import scraper.service.model.Pipeline
import scraper.service.model.Task

@Service
class PipelineRunnerService {

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    PipelineService pipelineService

    @Autowired
    TaskQueueService taskQueueService

    @Autowired
    TaskService taskService

    PendingApiTaskDto pendingFromApi(String pipelineId) {
        logger.info("pendingFromApi: ${pipelineId}")
        Pipeline pipeline = pipelineService.findById(pipelineId)
        if (!pipeline) {
            throw new Exception("pipeline with id: ${pipelineId} not found")
        }
        Task task = taskService.createFromPipeline(pipeline)
        def pendingApiTaskDto = TaskMapper.toPendingApiTaskDto(task)
        taskQueueService.toRunTaskQueue(pendingApiTaskDto)
        return pendingApiTaskDto
    }

    PendingTaskDto pendingFromClient(String pipelineId) {
        logger.info("pendingFromClient: ${pipelineId}")
        Pipeline pipeline = pipelineService.findById(pipelineId)
        if (!pipeline) {
            throw new Exception("pipeline with id: ${pipelineId} not found")
        }
        Task task = taskService.createFromPipeline(pipeline)
        def pendingTaskDto = TaskMapper.toPendingTaskDto(task)
        taskQueueService.toRunTaskQueue(pendingTaskDto)
        return pendingTaskDto
    }

    PendingTaskDto stoppingFromClient(String taskId) {
        logger.info("stoppingFromClient: ${taskId}")
        Task task = taskService.findById(taskId)
        if (!task) {
            throw new Exception("task id id: ${taskId} not found")
        }
        task.status = PipelineStatuses.STOPPING
        taskService.update(task)
        taskQueueService.toStopTaskQueue(taskId)
        return TaskMapper.toPendingTaskDto(task)
    }
}
