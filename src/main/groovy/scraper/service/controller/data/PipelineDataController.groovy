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
import scraper.service.repository.UserTokenRepository
import scraper.service.auth.TokenService

import javax.servlet.http.HttpServletRequest
import javax.xml.ws.http.HTTPException

@RestController
@RequestMapping("/api/pipeline")
class PipelineDataController {

    public static final String DEFAULT_BROWSER_ADDRESS = 'http://selenium.bars-parser.com:4444/wd/hub'
    public static final String DEFAULT_BROWSER = 'chrome'

    @Autowired
    UserTokenRepository userTokenRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    PipelineRepository pipelineRepository

    @Autowired
    PipelineTaskRepository pipelineTaskRepository

    @Autowired
    PipelineStatusRepository pipelineStatusRepository

    @Autowired
    TokenService tokenService

    @RequestMapping('/{id}')
    Pipeline get(@PathVariable String id) {
        return pipelineRepository.findOne(id)
    }

    @RequestMapping('/{id}/commands')
    Pipeline commands(@PathVariable String id) {
        Pipeline pipeline = get(id)
        String json = pipeline.jsonCommands
    }

    @RequestMapping('/list')
    List<Pipeline> list(@RequestHeader("token") String token) {
        String userId = tokenService.getUserId(token)
        return pipelineRepository.findByUser(userId)
    }

    @RequestMapping('/list-depends/{pipelineId}')
    List<Pipeline> listDepends(@RequestHeader("token") String token, @PathVariable String pipelineId) {
        String userId = tokenService.getUserId(token)
        def pipelines = pipelineRepository.findByUserAndIdNotIn(userId, [pipelineId])
        return pipelines
    }

    @RequestMapping('/save')
    Pipeline add(HttpServletRequest request, @RequestHeader String token, @RequestParam String id) {
        String userId = tokenService.getUserId(token)
        User user = userRepository.findOne(userId)
        if (!user) {
            throw HTTPException('user not found by passed token')
        }
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
        Integer runIntervalMin = (request.getParameter('runIntervalMin') ?: null) as Integer
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
            pipeline.runIntervalMin = runIntervalMin
            pipelineRepository.save(pipeline)
            return pipeline
        }
        PipelineStatus status = pipelineStatusRepository.findByTitle('not running')
        pipeline = new Pipeline(key: key, browser: DEFAULT_BROWSER, jsonCommands: jsonCommands,
                user: user, description: description, dependOn: dependOnPipeline, createdOn: new Date(),
                modifiedOn: new Date(), status: status, runIntervalMin: runIntervalMin,
                isTakeScreenshot: isTakeScreenshot, isDebugMode: isDebugMode, browserAddress: browserAddress)
        pipelineRepository.save(pipeline)
        return pipeline
    }

    @RequestMapping("/delete/{id}")
    void delete(@PathVariable String id) {
        pipelineRepository.delete(id)
    }
}
