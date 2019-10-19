package scraper.service.amqp.consumer

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import scraper.service.amqp.QueueConstants
import scraper.service.amqp.producer.PipelineProducer
import scraper.service.constants.PipelineStatuses
import scraper.service.model.Pipeline
import scraper.service.repository.PipelineStatusRepository
import scraper.service.service.PipelineService
import scraper.service.service.PipelineStructureService

@Component
class PipelineRunnerConsumer {

    @Autowired
    PipelineProducer pipelineProducer

    @Autowired
    SimpMessagingTemplate messagingTemplate

    @Autowired
    PipelineService pipelineService

    @Autowired
    PipelineStatusRepository pipelineStatusRepository

    @Autowired
    PipelineStructureService pipelineStructureService

    @Autowired
    QueueConstants queueConstants

    /**
     * Listener for run pipeline.
     * @param pipelineId Running pipeline pipelineId.
     */
    @RabbitListener(queues = '#{queueConstants.PIPELINE_RUN}', containerFactory="defaultConnectionFactory")
    void runPipelineFromQueueWorker(String pipelineId) {
        runPipelineFromQueue(pipelineId)
    }

    @RabbitListener(queues = '#{queueConstants.PIPELINE_STOP}', containerFactory="defaultConnectionFactory")
    void stopPipelineFromQueueWorker(String pipelineId) {
        pipelineService.stop(pipelineId)
    }

    /**
     * Build, created instance and runs pipeline by database pipelineId.
     * @param pipelineId Running pipeline pipelineId.
     */
    private void runPipelineFromQueue(String pipelineId) {
        pipelineService.run(pipelineId)
    }
}
