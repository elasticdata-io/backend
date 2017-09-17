package scraper.service.schedule

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import scraper.service.model.Pipeline
import scraper.service.model.PipelineStatus
import scraper.service.repository.PipelineRepository
import groovy.time.TimeCategory
import scraper.service.repository.PipelineStatusRepository

import javax.annotation.PostConstruct

@Component
class PipelineSchedule {

    private static final Logger logger = LoggerFactory.getLogger(PipelineSchedule.class)

    @Autowired
    AmqpTemplate rabbitTemplate

    @Autowired
    PipelineRepository pipelineRepository

    @Autowired
    PipelineStatusRepository pipelineStatusRepository

    private PipelineStatus runningStatus

    @PostConstruct
    void init() {
        runningStatus = pipelineStatusRepository.findByTitle('running')
    }

    @Scheduled(cron='0 * * * * *')
    void checkRunPipeline() {
        List<Pipeline> pipelines = pipelineRepository.findByStatusNot(runningStatus)
        pipelines.each { pipeline ->
            if (!pipeline.runIntervalMin) {
                return
            }
            pipeline.lastCompletedOn
            def now = new Date()
            use( TimeCategory ) {
                def needStartTime = pipeline.lastCompletedOn + pipeline.runIntervalMin.minutes
                if (now <= needStartTime) {
                    logger.info("run pipeline from schedule: ${pipeline.id}, time: ${now}")
                    rabbitTemplate.convertAndSend("pipeline-run-queue", pipeline.id)
                }
            }
        }
    }
}
