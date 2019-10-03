package scraper.service.controller

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import scraper.core.command.input.UserInput
import scraper.service.constants.PipelineStatuses
import scraper.service.consumer.QueueConstants
import scraper.service.data.converter.CsvDataConverter
import scraper.service.dto.mapper.PipelineMapper
import scraper.service.dto.model.user.PipelineDto
import scraper.service.model.Pipeline
import scraper.service.model.PipelineTask
import scraper.service.service.PipelineInputService
import scraper.service.service.PipelineService
import scraper.service.repository.PipelineRepository
import scraper.service.repository.PipelineStatusRepository
import scraper.service.repository.PipelineTaskRepository
import scraper.service.util.PipelineStructureService

import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/pipeline")
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
    PipelineStructureService pipelineStructure

    @Autowired
    CsvDataConverter csvConverter

    @Autowired
    PipelineService pipelineService

    @Autowired
    PipelineInputService pipelineInputService

    /**
     * Runs pipeline process by pipeline pipelineId.
     * @param id
     */
    @RequestMapping("/run/{id}")
    PipelineDto addToRunQueue(@PathVariable String id) {
        Pipeline pipeline = pipelineService.findById(id)
        String statusTitle = pipeline.status?.title
        if (statusTitle == PipelineStatuses.PENDING || statusTitle == PipelineStatuses.RUNNING) {
//            return
        }
        if (!pipeline) {
            return
        }
        def pendingStatus = pipelineStatusRepository.findByTitle(PipelineStatuses.PENDING)
        pipeline.status = pendingStatus
        pipelineRepository.save(pipeline)
        rabbitTemplate.convertAndSend(QueueConstants.PIPELINE_RUN, id)
        return PipelineMapper.toPipelineDto(pipeline)
    }

    /**
     * Stop pipeline process by pipeline pipelineId.
     * @param id
     */
    @RequestMapping("/stop/{id}")
    Pipeline stopPipeline(@PathVariable String id) {
        Pipeline pipeline = pipelineService.findById(id)
        def stoppingStatus = pipelineStatusRepository.findByTitle(PipelineStatuses.STOPPING)
        pipeline.status = stoppingStatus
        pipelineRepository.save(pipeline)
        rabbitTemplate.convertAndSend(QueueConstants.PIPELINE_STOP, id)
        return pipeline
    }

    /**
     * Gets last parsed data by pipeline pipelineId.
     * @param pipelineId
     * @return Last parsed data by pipeline pipelineId.
     */
    @RequestMapping("/data/{pipelineId}")
    List<HashMap> getData(@PathVariable String pipelineId) {
        PipelineTask pipelineTask = pipelineTaskRepository.findOneByPipelineAndErrorOrderByEndOnDesc(pipelineId, null)
        return pipelineTask.data as List<HashMap>
    }

    /**
     * Gets last parsed data by pipeline pipelineId.
     * @param pipelineId
     * @return Last parsed data by pipeline pipelineId.
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

    /**
     * Kill all chromedriver instances
     */
    @RequestMapping("/kill-all-chrome-driver")
    void killAllChromeDriver() {
        Runtime.getRuntime().exec('killall chromedriver')
    }

    @RequestMapping("/user-input/list/{pipelineId}")
    List<UserInput> listUserInput(@PathVariable String pipelineId) {
        return pipelineInputService.findUserInputs(pipelineId)
    }

    @RequestMapping(value = "/user-input/set-text/{pipelineId}/{key}", method = RequestMethod.POST)
    void setTextToUserInput(@PathVariable String pipelineId, @PathVariable String key, @RequestParam String text) {
        UserInput userInput = pipelineInputService.findUserInput(pipelineId, key)
        if (userInput) {
            userInput.text = text
        }
    }
}
