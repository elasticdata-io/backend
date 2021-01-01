package scraper.service

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.constants.PipelineStatuses
import scraper.dto.mapper.pipeline.PipelineMapper
import scraper.dto.mapper.TaskMapper
import scraper.dto.model.task.PendingTaskDto
import scraper.model.Pipeline
import scraper.repository.PipelineRepository
import scraper.ws.PipelineWebsocketProducer

@Service
class PipelineService {

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    private TaskService taskService

    @Autowired
    private PipelineWebsocketProducer pipelineWebsocketProducer

    @Autowired
    private PipelineRepository pipelineRepository

    Pipeline findById(String id) {
        Optional<Pipeline> pipeline = pipelineRepository.findById(id)
        return pipeline.present ? pipeline.get() : null
    }

    List<Pipeline> findByIds(List<String> ids) {
        return pipelineRepository.findByIdIn(ids)
    }

    void save(Pipeline pipeline) {
        pipelineRepository.save(pipeline)
    }

    void update(Pipeline pipeline) {
        save(pipeline)
        def pipelineDto = PipelineMapper.toPipelineDto(pipeline)
        pipelineWebsocketProducer.change(pipelineDto)
    }

    void updateFromTask(PendingTaskDto taskDto) {
        def pipeline = findById(taskDto.pipelineId)
        if (!pipeline) {
            return
        }
        logger.info("update pipeline from PendingTaskDto, taskDto = ${taskDto}")
        if (taskDto.endOnUtc > pipeline.lastCompletedOn) {
            pipeline.lastCompletedOn = taskDto.endOnUtc
            // pipeline.parseRowsCount = taskDto.docs.size()
            pipeline.status = taskDto.status
        }
        def tasksTotal = pipeline.tasksTotal ?: 0
        pipeline.tasksTotal = tasksTotal + 1
        pipeline.parseRowsCount = taskDto.docsCount
        pipeline.parseBytes = taskDto.docsBytes
        update(pipeline)
    }

    void updateFromTask(scraper.model.Task task) {
        PendingTaskDto taskDto = TaskMapper.toPendingTaskDto(task)
        updateFromTask(taskDto)
    }

    void validate(Pipeline pipeline) {
        // todo : check required props
        def pipelineConfiguration = pipeline.dsl
        println pipelineConfiguration
    }

    List<Pipeline> findInProcessing(String userId) {
        List<String> inProcessingStatuses = PipelineStatuses.getInProcessing()
        List<scraper.model.Task> tasks = taskService.findByStatusInAndUserId(inProcessingStatuses, userId)
        List<String> pipelineIds = tasks.collect { it.pipelineId }
        return findByIds(pipelineIds)
    }

    List<Pipeline> findAll(String userId) {
        return  pipelineRepository.findByUserOrderByModifiedOnDesc(userId)
    }
}
