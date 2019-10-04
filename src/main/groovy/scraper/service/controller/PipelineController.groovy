package scraper.service.controller

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import scraper.core.command.input.UserInput
import scraper.service.amqp.producer.PipelineProducer
import scraper.service.constants.PipelineStatuses
import scraper.service.dto.mapper.PipelineMapper
import scraper.service.dto.model.user.PipelineDto
import scraper.service.model.Pipeline
import scraper.service.service.PipelineInputService
import scraper.service.service.PipelineService
import scraper.service.repository.PipelineRepository
import scraper.service.repository.PipelineStatusRepository
import scraper.service.repository.PipelineTaskRepository
import scraper.service.service.PipelineStructureService

@RestController
@RequestMapping("/pipeline")
class PipelineController {

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    PipelineProducer pipelineProducer

    @Autowired
    PipelineRepository pipelineRepository

    @Autowired
    PipelineTaskRepository pipelineTaskRepository

    @Autowired
    PipelineStatusRepository pipelineStatusRepository

    @Autowired
    PipelineStructureService pipelineStructure

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
            logger.error("pipeline: ${pipeline.id} alredy ${statusTitle}")
//            return
        }
        if (!pipeline) {
            return
        }
        def pendingStatus = pipelineStatusRepository.findByTitle(PipelineStatuses.PENDING)
        pipeline.status = pendingStatus
        pipelineRepository.save(pipeline)
        pipelineProducer.run(id)
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
        pipelineProducer.stop(id)
        return pipeline
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
