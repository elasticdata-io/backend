package scraper.controller

import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import scraper.constants.PipelineStatuses
import scraper.dto.mapper.pipeline.PipelineDependencyMapper
import scraper.dto.mapper.pipeline.DslMapper
import scraper.dto.mapper.pipeline.PipelineMapper
import scraper.dto.model.pipeline.PipelineDto
import scraper.model.Pipeline
import scraper.model.Task
import scraper.model.User
import scraper.repository.PipelineRepository
import scraper.repository.UserRepository
import scraper.repository.UserTokenRepository
import scraper.service.PipelineService
import scraper.service.TaskService
import scraper.service.UserService
import scraper.service.auth.JwtTokenService
import scraper.service.converter.CsvDataConverter

import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/pipeline")
class PipelineDataController {

    @Autowired
    UserTokenRepository userTokenRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    PipelineRepository pipelineRepository

    @Autowired
    PipelineService pipelineService

    @Autowired
    TaskService taskService

    @Autowired
    UserService userService

    @Autowired
    JwtTokenService tokenService

    @Autowired
    CsvDataConverter csvConverter

    @GetMapping('/{id}')
    PipelineDto get(@PathVariable String id) {
        Pipeline pipeline = pipelineService.findById(id)
        if (!pipeline) {
            return
        }
        return PipelineMapper.toPipelineDto(pipeline)
    }

    @GetMapping('/{id}/commands')
    Pipeline commands(@PathVariable String id) {
        PipelineDto pipeline = get(id)
        String json = pipeline.jsonCommands
    }

    @GetMapping('/list')
    List<PipelineDto> list(@RequestHeader("token") String token) {
        String userId = tokenService.getUserId(token)
        List<Pipeline> pipelines = pipelineRepository.findByUserOrderByModifiedOnDesc(userId)
        return pipelines.collect {pipeline ->
            return PipelineMapper.toPipelineDto(pipeline)
        }
    }

    @GetMapping('/list/in-processing')
    List<PipelineDto> listInProcessing(@RequestHeader("token") String token) {
        String userId = tokenService.getUserId(token)
        List<String> inProcessingStatuses = PipelineStatuses.getInProcessing()
        List<Task> tasks = taskService.findByStatusInAndUserId(inProcessingStatuses, userId)
        List<String> pipelineIds = tasks.collect { it.pipelineId }
        List<Pipeline> pipelines = pipelineService.findByIds(pipelineIds)
        return pipelines.collect {pipeline ->
            return PipelineMapper.toPipelineDto(pipeline)
        }
    }

    @GetMapping('/list-depends/{pipelineId}')
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
        pipeline.pipelineVersion = pipelineDto.pipelineVersion
        pipeline.needProxy = pipelineDto.needProxy
        pipeline.dependencies = PipelineDependencyMapper.toPipelineDependencies(pipelineDto.dependencies)
        pipeline.hookUrl = pipelineDto.hookUrl
        pipeline.dsl = DslMapper.toDslEntity(pipelineDto.dsl)
        if (!pipelineInDb) {
            pipeline.status = PipelineStatuses.NOT_RUNNING
            pipeline.user = user
            pipeline.isDebugMode = false
        }
        pipelineService.validate(pipeline)
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
        clonePipeline.lastStartedOn = null
        clonePipeline.lastCompletedOn = null
        clonePipeline.status = PipelineStatuses.NOT_RUNNING
        pipelineService.save(clonePipeline)
        return PipelineMapper.toPipelineDto(clonePipeline)
    }

    /**
     * Gets last parsed data by pipeline pipelineId.
     * @param pipelineId
     * @return Last parsed data by pipeline pipelineId.
     */
    @GetMapping("/data/{pipelineId}")
    List<HashMap> getData(@PathVariable String pipelineId) {
        Optional<Pipeline> pipelineOptional = pipelineRepository.findById(pipelineId)
        if (!pipelineOptional.present) {
            throw new Exception("pipeline with id ${pipelineId} not found")
        }
        Task task = taskService.findLastCompletedTask(pipelineId)
        if (!task) {
            return new ArrayList<HashMap>()
        }
        // return fileDataRepository.getDataFileToList(task)
    }

    /**
     * Gets last parsed data by pipeline pipelineId.
     * @param pipelineId
     * @return Last parsed data by pipeline pipelineId.
     */
    @GetMapping("/data/csv/{pipelineId}")
    List<HashMap> getCsvData(@PathVariable String pipelineId, HttpServletResponse response) {
        PageRequest page = PageRequest.of(0, 1)
        List<Task> tasks = taskService
                .findByPipelineAndErrorOrderByStartOnDesc(pipelineId, null, page)
        if (tasks.size() == 0) {
            return
        }
//        List<HashMap> list = fileDataRepository.getDataFileToList(tasks.first()) as List<HashMap>
//        String responseData = csvConverter.toCsv(list)
//        response.setContentType("text/csv; charset=utf-8")
//        response.setHeader("Content-disposition", "attachment;filename=${pipelineId}.csv")
//        response.getWriter().print(responseData)
    }

    @GetMapping("uuid")
    String uuid() {
        ObjectId id = new ObjectId()
        return id.toString()
    }
}
