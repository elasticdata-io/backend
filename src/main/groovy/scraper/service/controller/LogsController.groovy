package scraper.service.controller

import groovy.io.FileType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import scraper.service.auth.TokenService
import scraper.service.model.PipelineTask
import scraper.service.repository.*

@RestController
@RequestMapping("/pipeline/log")
class LogsController {

    @Autowired
    PipelineRepository pipelineRepository

    @Autowired
    PipelineTaskRepository pipelineTaskRepository

    @Autowired
    PipelineStatusRepository pipelineStatusRepository

    @Autowired
    TokenService tokenService

    @Value('${static.runDirectory}')
    private String RUN_DIRECTORY

    @RequestMapping('/{pipelineTaskId}')
    String logs(@PathVariable String pipelineTaskId) {
        Optional<PipelineTask> pipelineTask = pipelineTaskRepository.findById(pipelineTaskId)
        if (pipelineTask.present) {
            def task = pipelineTask.get()
            String tmpFolder = "${RUN_DIRECTORY}/${task.id}"
            return new File("${tmpFolder}/pipeline-${task.id}.log").text
        }
    }

    @RequestMapping('/screenshots/{pipelineTaskId}')
    String[] screenshots(@PathVariable String pipelineTaskId) {
        Optional<PipelineTask> pipelineTask = pipelineTaskRepository.findById(pipelineTaskId)
        def list = []
        if (pipelineTask.present) {
            def task = pipelineTask.get()
            String tmpFolder = "${RUN_DIRECTORY}/${task.id}"
            def dir = new File("${tmpFolder}/screenshots")
            if (dir.exists()) {
                dir.eachFileRecurse (FileType.FILES) { file ->
                    list << "http://applogs.elasticdata.io/${task.id}/screenshots/${file.name}"
                }
            }
        }
        return list
    }
}
