package scraper.service.controller

import groovy.io.FileType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import scraper.service.auth.TokenService
import scraper.service.model.Task
import scraper.service.repository.*
import scraper.service.service.TaskService

@RestController
@RequestMapping("/pipeline/log")
class LogsController {

    @Autowired
    PipelineRepository pipelineRepository

    @Autowired
    TaskService taskService

    @Autowired
    TokenService tokenService

    @Value('${static.runDirectory}')
    private String RUN_DIRECTORY

    @RequestMapping('/{pipelineTaskId}')
    String logs(@PathVariable String pipelineTaskId) {
        Task task = taskService.findById(pipelineTaskId)
        if (task) {
            String tmpFolder = "${RUN_DIRECTORY}/${task.id}"
            def file = new File("${tmpFolder}/pipeline-${task.id}.log")
            if (file.exists()) {
                return file.text
            }
        }
        return 'Logs not founds...'
    }

    @RequestMapping('/screenshots/{pipelineTaskId}')
    String[] screenshots(@PathVariable String pipelineTaskId) {
        Task task = taskService.findById(pipelineTaskId)
        def list = []
        if (task) {
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
