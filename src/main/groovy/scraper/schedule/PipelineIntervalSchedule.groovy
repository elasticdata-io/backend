package scraper.schedule

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import scraper.constants.PipelineStatuses
import scraper.model.Pipeline
import scraper.repository.PipelineRepository
import groovy.time.TimeCategory
import scraper.service.TaskService

@Component
class PipelineIntervalSchedule {

    @Autowired
    TaskService taskService

    @Autowired
    PipelineRepository pipelineRepository

    @Scheduled(cron='0/10 * * * * *')
    void checkRunPipeline() {
        List<Pipeline> pipelines = pipelineRepository.findByRunIntervalMinIsNotNull()
        pipelines.each { pipeline ->
            def lastTask  = taskService.findLastTask(pipeline.id)
            def currentStatus = lastTask ? lastTask.status : ''
            def taskIsPrecessing = PipelineStatuses.isTaskProcessing(currentStatus)
            if (taskIsPrecessing) {
                return
            }
            def lastCompletedOn = lastTask ? lastTask.endOnUtc : new Date('2000-01-01')
            def now = (new Date())
            use( TimeCategory ) {
                def minutes = (pipeline.runIntervalMin as int).minutes
                def needStartTime = lastCompletedOn + minutes
                if (now >= needStartTime) {
                    taskService.createAndRun(pipeline)
                }
            }
        }
    }
}
