package scraper.service.controller

import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import scraper.service.constants.PipelineStatuses
import scraper.service.dto.mapper.PipelineDependencyMapper
import scraper.service.dto.mapper.PipelineMapper
import scraper.service.dto.model.pipeline.PipelineDependencyDto
import scraper.service.dto.model.pipeline.PipelineDto
import scraper.service.model.Pipeline
import scraper.service.model.PipelineStatus
import scraper.service.model.PipelineTask
import scraper.service.model.User
import scraper.service.repository.PipelineRepository
import scraper.service.repository.PipelineStatusRepository
import scraper.service.repository.PipelineTaskRepository
import scraper.service.repository.UserRepository
import scraper.service.repository.UserTokenRepository
import scraper.service.auth.TokenService
import scraper.service.service.PipelineService
import scraper.service.service.UserService
import scraper.service.service.converter.CsvDataConverter

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/pipeline")
class PipelineDataController {

    @Value('${selenium.default.browser}')
    String SELENIUM_DEFAULT_BROWSER

    @Autowired
    UserTokenRepository userTokenRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    PipelineRepository pipelineRepository

    @Autowired
    PipelineService pipelineService

    @Autowired
    PipelineTaskRepository pipelineTaskRepository

    @Autowired
    PipelineStatusRepository pipelineStatusRepository

    @Autowired
    UserService userService

    @Autowired
    TokenService tokenService

    @Autowired
    CsvDataConverter csvConverter

    @RequestMapping('/{id}')
    PipelineDto get(@PathVariable String id) {
        Pipeline pipeline = pipelineService.findById(id)
        if (!pipeline) {
            return
        }
        pipeline.status = pipeline.status ?: new PipelineStatus()
        return PipelineMapper.toPipelineDto(pipeline)
    }

    @RequestMapping('/{id}/commands')
    Pipeline commands(@PathVariable String id) {
        PipelineDto pipeline = get(id)
        String json = pipeline.jsonCommands
    }

    @RequestMapping('/list')
    List<PipelineDto> list(@RequestHeader("token") String token) {
        String userId = tokenService.getUserId(token)
        List<Pipeline> pipelines = pipelineRepository.findByUserOrderByCreatedOnDesc(userId)
        pipelines.forEach({ pipeline ->
            pipeline.status = pipeline.status ?: new PipelineStatus()
        })
        return pipelines.collect {pipeline ->
            return PipelineMapper.toPipelineDto(pipeline)
        }
    }

    @RequestMapping('/list-depends/{pipelineId}')
    List<PipelineDto> listDepends(@RequestHeader("token") String token, @PathVariable String pipelineId) {
        String userId = tokenService.getUserId(token)
        def pipelines = pipelineRepository.findByUserAndIdNotIn(userId, [pipelineId])
        return pipelines.collect {pipeline ->
            return PipelineMapper.toPipelineDto(pipeline)
        }
    }

    @PostMapping('/save')
    PipelineDto add(@RequestHeader String token,
                    @RequestBody PipelineDto pipelineDto) {
        String userId = tokenService.getUserId(token)
        User user = userService.findById(userId)
        if (!user) {
            throw new Exception('user not found by passed token')
        }
        def pipelineInDb = pipelineService.findById(pipelineDto.id)
        Pipeline pipeline = pipelineInDb ?: PipelineMapper.toPipeline(pipelineDto)
        pipeline.isTakeScreenshot = pipelineDto.isTakeScreenshot
        pipeline.isDebugMode = pipelineDto.isDebugMode
        pipeline.key = pipelineDto.key
        pipeline.description = pipelineDto.description
        pipeline.jsonCommands = pipelineDto.jsonCommands
        pipeline.modifiedOn = new Date()
        pipeline.needProxy = pipelineDto.needProxy
        pipeline.dependencies = PipelineDependencyMapper.toPipelineDependencies(pipelineDto.dependencies)
        pipeline.modifiedOn = new Date()
        if (!pipelineInDb) {
            PipelineStatus status = pipelineStatusRepository.findByTitle('not running')
            pipeline.status = status
            pipeline.createdOn = new Date()
            pipeline.user = user
            pipeline.browser = SELENIUM_DEFAULT_BROWSER
            pipeline.isDebugMode = true
        }
        pipelineRepository.save(pipeline)
        return PipelineMapper.toPipelineDto(pipeline)
    }

    @DeleteMapping("/delete/{id}")
    void delete(@PathVariable String id) {
        def pipeline = pipelineRepository.findById(id)
        pipelineRepository.delete(pipeline.get())
    }

    @PostMapping("/clone/{id}")
    PipelineDto clone(@PathVariable String id) {
        Optional<Pipeline> pipelineOptional = pipelineRepository.findById(id)
        if (!pipelineOptional.present) {
            return
        }
        Pipeline clonePipeline = pipelineOptional.get()
        clonePipeline.id = null
        clonePipeline.key = "${clonePipeline.key} (копія)"
        clonePipeline.tasksTotal = 0
        clonePipeline.parseRowsCount = 0
        clonePipeline.runIntervalMin = 0
        clonePipeline.createdOn = new Date()
        clonePipeline.modifiedOn = new Date()
        clonePipeline.lastStartedOn = null
        clonePipeline.lastCompletedOn = null
        clonePipeline.status = pipelineStatusRepository.findByTitle(PipelineStatuses.NOT_RUNNING)
        pipelineService.save(clonePipeline)
        return PipelineMapper.toPipelineDto(clonePipeline)
    }

    /**
     * Gets last parsed data by pipeline pipelineId.
     * @param pipelineId
     * @return Last parsed data by pipeline pipelineId.
     */
    @RequestMapping("/data/{pipelineId}")
    List<HashMap> getData(@PathVariable String pipelineId) {
        PageRequest page = new PageRequest(0, 1)
        List<PipelineTask> pipelineTasks = pipelineTaskRepository
                .findOneByPipelineAndErrorOrderByStartOnDesc(pipelineId, null, page)
        if (pipelineTasks.size() == 0) {
            return
        }
        return pipelineTasks.first().data as List<HashMap>
    }

    /**
     * Gets last parsed data by pipeline pipelineId.
     * @param pipelineId
     * @return Last parsed data by pipeline pipelineId.
     */
    @RequestMapping("/data/csv/{pipelineId}")
    List<HashMap> getCsvData(@PathVariable String pipelineId, HttpServletResponse response) {
        PageRequest page = new PageRequest(0, 1)
        List<PipelineTask> pipelineTasks = pipelineTaskRepository
                .findOneByPipelineAndErrorOrderByStartOnDesc(pipelineId, null, page)
        if (pipelineTasks.size() == 0) {
            return
        }
        List<HashMap> list = pipelineTasks.first().data as List<HashMap>
        String responseData = csvConverter.toCsv(list)
        response.setContentType("text/csv; charset=utf-8")
        response.setHeader("Content-disposition", "attachment;filename=${pipelineId}.csv")
        response.getWriter().print(responseData)
    }

    @GetMapping("uuid")
    String uuid() {
        ObjectId id = new ObjectId()
        return id.toString()
    }
}
