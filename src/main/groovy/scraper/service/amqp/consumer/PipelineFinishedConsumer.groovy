package scraper.service.amqp.consumer

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.service.amqp.QueueConstants
import scraper.service.service.PipelineRunnerService
import scraper.service.service.PipelineService

@Component
class PipelineFinishedConsumer {

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    QueueConstants queueConstants

    @Autowired
    PipelineService pipelineService

    @Autowired
    PipelineRunnerService pipelineRunnerService

    /**
     * Listener for run pipelineTask.
     * @param pipelineTaskId Id of the finished task pipelineId.
     */
    @RabbitListener(queues = '#{queueConstants.PIPELINE_FINISHED}', containerFactory="defaultConnectionFactory")
    void worker(String pipelineId) {
        logger.info("finished pipeline: ${pipelineId}")
        def pipelines = pipelineService.findByDependenciesAndStatusWaiting(pipelineId)
        if (pipelines.size() == 1) {
            def pipeline = pipelines.first()
            logger.info("run deps pipeline: ${pipeline.id}")
            pipelineRunnerService.needRunFromFinishedDependencies(pipeline.id)
        }
    }
}
