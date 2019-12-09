package scraper.service.service

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.core.pipeline.PipelineBuilder
import scraper.core.pipeline.data.storage.FileStoreProvider
import scraper.service.dto.mapper.PipelineMapper
import scraper.service.dto.model.task.PendingTaskDto
import scraper.service.model.Pipeline
import scraper.service.repository.PipelineRepository
import scraper.service.ws.PipelineWebsocketProducer

@Service
class PipelineService {

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    private PipelineWebsocketProducer pipelineWebsocketProducer

    @Autowired
    private FileStoreProvider fileStoreProvider

    @Autowired
    private PipelineRepository pipelineRepository

    Pipeline findById(String id) {
        Optional<Pipeline> pipeline = pipelineRepository.findById(id)
        return pipeline.present ? pipeline.get() : null
    }

    void save(Pipeline pipeline) {
        pipeline.modifiedOn = new Date()
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
        update(pipeline)
    }

    void validate(Pipeline pipeline) {
        PipelineBuilder pipelineBuilder = new PipelineBuilder()
        pipelineBuilder.validateJson(pipeline.jsonCommands)
    }
}
