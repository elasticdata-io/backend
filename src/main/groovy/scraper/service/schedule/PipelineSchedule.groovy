package scraper.service.schedule

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.service.constants.PipelineStatuses
import scraper.service.model.Pipeline
import scraper.service.repository.PipelineRepository
import groovy.time.TimeCategory

@Component
class PipelineSchedule {

    private static final Logger logger = LoggerFactory.getLogger(PipelineSchedule.class)

    @Autowired
    AmqpTemplate rabbitTemplate

    @Autowired
    PipelineRepository pipelineRepository

//    @Scheduled(cron='0 * * * * *')
    void checkRunPipeline() {
        List<Pipeline> pipelines = pipelineRepository.findByStatusNot(PipelineStatuses.RUNNING)
        pipelines.each { pipeline ->
            if (!pipeline.runIntervalMin) {
                return
            }
            def lastCompletedOn = pipeline.lastCompletedOn
            def now = (new Date())
            use( TimeCategory ) {
                def needStartTime = lastCompletedOn + pipeline.runIntervalMin.minutes
                if (now >= needStartTime) {
                    logger.info("run pipeline from schedule: ${pipeline.id}, time: ${now}")
                    rabbitTemplate.convertAndSend("pipeline-run", pipeline.id)
                }
            }
        }
    }
}
