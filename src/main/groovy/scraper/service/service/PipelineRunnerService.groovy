package scraper.service.service

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.service.amqp.producer.PipelineProducer
import scraper.service.constants.PipelineStatuses
import scraper.service.dto.mapper.PipelineMapper
import scraper.service.dto.model.pipeline.PipelineDto
import scraper.service.model.Pipeline
import scraper.service.model.PipelineDependency
import scraper.service.repository.PipelineRepository
import scraper.service.repository.PipelineStatusRepository
import scraper.service.repository.PipelineTaskRepository

import java.time.Duration

@Service
class PipelineRunnerService {

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    PipelineStatusRepository pipelineStatusRepository

    @Autowired
    PipelineTaskRepository pipelineTaskRepository

    @Autowired
    PipelineProducer pipelineProducer

    @Autowired
    PipelineRepository pipelineRepository

    @Autowired
    PipelineService pipelineService

    /**
     * @param pipelineId
     * @return
     */
    PipelineDto needRunFromFinishedDependencies(String pipelineId) {
        Pipeline pipeline = pipelineService.findById(pipelineId)
        String statusTitle = pipeline.status?.title
        if (!pipeline) {
            return
        }
        if (statusTitle == PipelineStatuses.PENDING
                || statusTitle == PipelineStatuses.RUNNING
                || statusTitle == PipelineStatuses.STOPPING) {
            logger.error("pipeline: ${pipeline.id} alredy ${statusTitle}")
            return
        }
        def pendingStatus = pipelineStatusRepository.findByTitle(PipelineStatuses.PENDING)
        pipeline.status = pendingStatus
        pipelineRepository.save(pipeline)
        pipelineProducer.run(pipelineId)
        return PipelineMapper.toPipelineDto(pipeline)
    }

    /**
     * @param pipelineId
     * @return
     */
    PipelineDto needRunFromClient(String pipelineId) {
        Pipeline pipeline = pipelineService.findById(pipelineId)
        String statusTitle = pipeline.status?.title
        if (!pipeline) {
            return
        }
        if (statusTitle == PipelineStatuses.PENDING
                || statusTitle == PipelineStatuses.RUNNING
                || statusTitle == PipelineStatuses.STOPPING
                || statusTitle == PipelineStatuses.WAIT_OTHER_PIPELINE) {
            logger.error("pipeline: ${pipeline.id} alredy ${statusTitle}")
            return
        }
        def dependencies = pipeline.dependencies
        List<String> dependenciesTasks = findDependenciesTasks(dependencies)
        if (dependenciesTasks.size()) {
            dependenciesTasks.each{x ->
                needRunFromClient(x)
            }
            def waitingStatus = pipelineStatusRepository.findByTitle(PipelineStatuses.WAIT_OTHER_PIPELINE)
            pipeline.status = waitingStatus
            pipelineRepository.save(pipeline)
            return PipelineMapper.toPipelineDto(pipeline)
        }
        def pendingStatus = pipelineStatusRepository.findByTitle(PipelineStatuses.PENDING)
        pipeline.status = pendingStatus
        pipelineRepository.save(pipeline)
        pipelineProducer.run(pipelineId)
        return PipelineMapper.toPipelineDto(pipeline)
    }

    private List<String> findDependenciesTasks(List<PipelineDependency> dependencies) {
        def needRunPipelines = new ArrayList<String>()
        dependencies.each{x ->
            if (!x.dataFreshnessInterval) {
                needRunPipelines.add(x.pipelineId)
                return
            }
            def task = pipelineTaskRepository.findFirstByPipelineAndErrorOrderByEndOnDesc(x.pipelineId, null)
            Duration requireDuration = getDurationFromInterval(x.dataFreshnessInterval)
            def now = new Date()
            def taskDuration = now.time - task.endOn.time
            if (taskDuration > requireDuration.toMillis()) {
                needRunPipelines.add(x.pipelineId)
            }
        }
        return needRunPipelines
    }

    private static Duration getDurationFromInterval(String interval) {
        def value = interval.replaceAll('[^0-9]', '') as int
        def unit = interval.replaceAll('[0-9]', '')
        switch (unit) {
            case 's':
                return Duration.ofSeconds(value)
            case 'm':
                return Duration.ofMinutes(value)
            case 'h':
                return Duration.ofHours(value)
            case 'd':
                return Duration.ofDays(value)
        }
    }
}
