package scraper.service.consumer

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import scraper.service.constants.PipelineStatuses
import scraper.service.model.Pipeline
import scraper.service.repository.PipelineStatusRepository
import scraper.service.service.PipelineService
import scraper.service.util.PipelineStructureService

@Component
class PipelineRunnerConsumer {

    @Autowired
    RabbitTemplate rabbitTemplate

    @Autowired
    SimpMessagingTemplate messagingTemplate

    @Autowired
    PipelineService pipelineService

    @Autowired
    PipelineStatusRepository pipelineStatusRepository

    @Autowired
    PipelineStructureService pipelineStructureService

    @Value('${spring.rabbitmq.topicExchangeName}')
    String topicExchangeName

    /**
     * Listener for run pipeline.
     * @param pipelineId Running pipeline pipelineId.
     */
    @RabbitListener(queues = QueueConstants.PIPELINE_RUN, containerFactory="defaultConnectionFactory")
    void runPipelineFromQueueWorker(String pipelineId) {
        runPipelineFromQueue(pipelineId)
    }

    @RabbitListener(queues = QueueConstants.PIPELINE_STOP, containerFactory="defaultConnectionFactory")
    void stopPipelineFromQueueWorker(String pipelineId) {
        pipelineService.stop(pipelineId)
    }

    /**
     * Runs hierarchy dependents pipelines.
     * @param hierarchy
     */
    @RabbitListener(queues = QueueConstants.PIPELINE_RUN_HIERARCHY, containerFactory="defaultConnectionFactory")
    void runDependentsHierarchyPipelines(List<String> hierarchy) {
        String pipelineId = hierarchy.remove(0)
        pipelineService.run(pipelineId)
        Pipeline pipeline = pipelineService.findById(pipelineId)
        if (pipeline.status.title == PipelineStatuses.ERROR) {
            // notify pipeline not running because dependents pipeline has error
            String firstPipelineId = hierarchy.last()
            Pipeline firstPipeline = pipelineService.findById(firstPipelineId)
            firstPipeline.status = pipelineStatusRepository.findByTitle(PipelineStatuses.ERROR)
            pipelineService.save(firstPipeline)
            String channel = '/pipeline/change/' + pipeline.user.id
            messagingTemplate.convertAndSend(channel, firstPipeline)
            return
        }
        if (hierarchy.size() > 0) {
            rabbitTemplate.convertAndSend(topicExchangeName, QueueConstants.PIPELINE_RUN_HIERARCHY, hierarchy)
        }
    }

    /**
     * Build, created instance and runs pipeline by database pipelineId.
     * @param pipelineId Running pipeline pipelineId.
     */
    private void runPipelineFromQueue(String pipelineId) {
        List<String> hierarchy = pipelineStructureService.getPipelineHierarchy(pipelineId)
        if (hierarchy.size() > 1) {
            rabbitTemplate.convertAndSend(topicExchangeName, QueueConstants.PIPELINE_RUN_HIERARCHY, hierarchy.reverse())
            return
        }
        pipelineService.run(pipelineId)
    }
}
