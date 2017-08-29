package scraper.service.controller.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import scraper.service.model.Pipeline
import scraper.service.model.PipelineStatus
import scraper.service.model.User
import scraper.service.repository.PipelineRepository
import scraper.service.repository.PipelineStatusRepository
import scraper.service.repository.PipelineTaskRepository
import scraper.service.repository.UserRepository

import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/pipeline")
class PipelineDataController {

    public static final String DEFAULT_BROWSER_ADDRESS = 'http://selenium.bars-parser.com:4444/wd/hub'
    public static final String DEFAULT_BROWSER = 'phantom'

    @Autowired
    UserRepository userRepository

    @Autowired
    PipelineRepository pipelineRepository

    @Autowired
    PipelineTaskRepository pipelineTaskRepository

    @Autowired
    PipelineStatusRepository pipelineStatusRepository

    @RequestMapping('/{id}')
    Pipeline get(@PathVariable String id) {
        return pipelineRepository.findOne(id)
    }

    @RequestMapping('/list')
    List<Pipeline> list(@RequestHeader("token") String token) {
        User user = userRepository.findByToken(token)
        return pipelineRepository.findByUser(user)
    }

    @RequestMapping('/list-depends/{pipelineId}')
    List<Pipeline> listDepends(@RequestHeader("token") String token, @PathVariable String pipelineId) {
        User user = userRepository.findByToken(token)
        def pipelines = pipelineRepository.findByUserAndIdNotIn(user, [pipelineId])
        return pipelines
    }

    @RequestMapping('/save')
    Pipeline add(HttpServletRequest request, @RequestHeader String token, @RequestParam String id) {
        User user = userRepository.findByToken(token)
        Pipeline pipeline = pipelineRepository.findOne(id)
        String key = request.getParameter('key')
        String description = request.getParameter('description')
        String dependOn = request.getParameter('dependOn')
        Pipeline dependOnPipeline = dependOn ? pipelineRepository.findOne(dependOn) : null
        String jsonCommands = request.getParameter('jsonCommands')
        boolean isTakeScreenshot = request.getParameter('isTakeScreenshot') ?: false
        boolean isDebugMode = request.getParameter('isDebugMode') ?: false
        String browserAddress = request.getParameter('browserAddress') ?: DEFAULT_BROWSER_ADDRESS
        String browser = request.getParameter('browser') ?: DEFAULT_BROWSER
        if (pipeline) {
            pipeline.browser = browser
            pipeline.browserAddress = browserAddress
            pipeline.isTakeScreenshot = isTakeScreenshot
            pipeline.isDebugMode = isDebugMode
            pipeline.key = key
            pipeline.description = description
            pipeline.dependOn = dependOnPipeline
            pipeline.jsonCommands = jsonCommands
            pipeline.modifiedOn = new Date()
            pipelineRepository.save(pipeline)
            return pipeline
        }
        PipelineStatus status = pipelineStatusRepository.findByTitle('not running')
        pipeline = new Pipeline(key: key, browser: DEFAULT_BROWSER, jsonCommands: jsonCommands,
                user: user, description: description, dependOn: dependOnPipeline, createdOn: new Date(),
                modifiedOn: new Date(), status: status,
                isTakeScreenshot: isTakeScreenshot, isDebugMode: isDebugMode, browserAddress: browserAddress)
        pipelineRepository.save(pipeline)
        return pipeline
    }

    @RequestMapping("/delete/{id}")
    void delete(@PathVariable String id) {
        pipelineRepository.delete(id)
    }
}
