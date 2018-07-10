package scraper.service.controller

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import scraper.service.data.converter.CsvDataConverter
import scraper.service.model.Pipeline
import scraper.service.model.PipelineTask
import scraper.service.pipeline.PipelineService
import scraper.service.repository.PipelineRepository
import scraper.service.repository.PipelineStatusRepository
import scraper.service.repository.PipelineTaskRepository
import scraper.service.util.PipelineStructure

import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api/pipeline")
class PipelineController {

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    PipelineRepository pipelineRepository

    @Autowired
    PipelineTaskRepository pipelineTaskRepository

    @Autowired
    PipelineStatusRepository pipelineStatusRepository

    @Autowired
    AmqpTemplate rabbitTemplate

    @Autowired
    SimpMessagingTemplate messagingTemplate

    @Autowired
    PipelineStructure pipelineStructure

    @Autowired
    CsvDataConverter csvConverter

    @Autowired
    PipelineService pipelineService

    /**
     * Listener for run pipeline.
     * @param pipelineId Running pipeline id.
     */
    @RabbitListener(queues = "pipeline-run-queue", containerFactory="multipleListenerContainerFactory")
    void runPipelineFromQueueWorker(String pipelineId) {
        runPipelineFromQueue(pipelineId)
    }

    /**
     * Runs hierarchy dependents pipelines.
     * @param hierarchy
     */
    @RabbitListener(queues = "pipeline-run-hierarchy")
    void runDependentsHierarchyPipelines(List<String> hierarchy) {
        String pipelineId = hierarchy.remove(0)
        Pipeline pipeline = pipelineService.run(pipelineId)
        messagingTemplate.convertAndSend("/pipeline/change", pipeline)
        if (hierarchy.size() > 0) {
            rabbitTemplate.convertAndSend("pipeline-run-hierarchy", hierarchy)
        }
    }

    /**
     * Build, created instance and runs pipeline by database id.
     * @param pipelineId Running pipeline id.
     */
    private void runPipelineFromQueue(String pipelineId) {
        List<String> hierarchy = pipelineStructure.getPipelineHierarchy(pipelineId)
        if (hierarchy.size() > 1) {
            rabbitTemplate.convertAndSend("pipeline-run-hierarchy", hierarchy.reverse())
            return
        }
        Pipeline pipeline = pipelineService.run(pipelineId)
        messagingTemplate.convertAndSend("/pipeline/change", pipeline)
    }

    /**
     * Runs pipeline process by pipeline id.
     * @param id
     */
    @RequestMapping("/run/{id}")
    void addToRunQueue(@PathVariable String id) {
        Pipeline pipeline = pipelineRepository.findOne(id)
        if (!pipeline){}
        rabbitTemplate.convertAndSend("pipeline-run-queue", id)
    }

    /**
     * Stop pipeline process by pipeline id.
     * @param id
     */
    @RequestMapping("/stop/{id}")
    void stopPipeline(@PathVariable String id) {
        pipelineService.stop(id)
    }

    /**
     * Gets last parsed data by pipeline id.
     * @param pipelineId
     * @return Last parsed data by pipeline id.
     */
    @RequestMapping("/data/{pipelineId}")
    List<HashMap> getData(@PathVariable String pipelineId) {
        PipelineTask pipelineTask = pipelineTaskRepository.findOneByPipelineAndErrorOrderByEndOnDesc(pipelineId, null)
        return pipelineTask.data
    }

    /**
     * Gets last parsed data by pipeline id.
     * @param pipelineId
     * @return Last parsed data by pipeline id.
     */
    @RequestMapping("/data/csv/{pipelineId}")
    List<HashMap> getCsvData(@PathVariable String pipelineId, HttpServletResponse response) {
        PipelineTask pipelineTask = pipelineTaskRepository.findOneByPipelineAndErrorOrderByEndOnDesc(pipelineId, null)
        List<HashMap> list = pipelineTask.data as List<HashMap>
        String responseData = csvConverter.toCsv(list)
        response.setContentType("text/csv; charset=utf-8")
        response.setHeader("Content-disposition", "attachment;filename=${pipelineId}.csv")
        response.getWriter().print(responseData)
    }
}
