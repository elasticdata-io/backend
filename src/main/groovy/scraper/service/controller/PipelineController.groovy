package scraper.service.controller

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import scraper.core.browser.Browser
import scraper.core.browser.BrowserFactory
import scraper.core.browser.provider.Phantom
import scraper.core.pipeline.Environment
import scraper.core.pipeline.PipelineBuilder
import scraper.core.pipeline.PipelineProcess
import scraper.core.pipeline.data.AbstractStore
import scraper.core.pipeline.data.Store
import scraper.service.constants.PipelineStatuses
import scraper.service.controller.listener.PipelineStoreObserver
import scraper.service.model.Pipeline
import scraper.service.model.PipelineTask
import scraper.service.repository.PipelineRepository
import scraper.service.repository.PipelineStatusRepository
import scraper.service.repository.PipelineTaskRepository
import scraper.service.util.ElasticSearchService
import scraper.service.util.PipelineStructure

import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api/pipeline")
class PipelineController {

    private static String TEMP_DIRECTORY = '/tmp/scraper-service'
    private static Class DEFAULT_BROWSER = Phantom.class
    public static final String  = System.getProperty('java.io.tmpdir')

    @Autowired
    ElasticSearchService elasticSearchService

    @Autowired
    PipelineRepository pipelineRepository

    @Autowired
    PipelineTaskRepository pipelineTaskRepository

    @Autowired
    PipelineStatusRepository pipelineStatusRepository

    @Autowired
    AmqpTemplate rabbitTemplate

    @Autowired
    private SimpMessagingTemplate messagingTemplate

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    PipelineStructure pipelineStructure

    Logger logger = LogManager.getRootLogger()

    DefaultListableBeanFactory beanFactory

    @PostConstruct
    void initialise() {
        def configurableContext = ((ConfigurableApplicationContext) applicationContext)
        beanFactory = configurableContext.getBeanFactory() as DefaultListableBeanFactory
    }

    /**
     * WORKER 1.
     * Listener for run pipeline.
     * @param pipelineId Running pipeline id.
     */
    @RabbitListener(queues = "pipeline-run-queue")
    void runPipelineFromQueueWorker1(String pipelineId) {
        runPipelineFromQueue(pipelineId)
    }

    /**
     * WORKER 2.
     * Listener for run pipeline.
     * @param pipelineId Running pipeline id.
     */
    @RabbitListener(queues = "pipeline-run-queue")
    void runPipelineFromQueueWorker2(String pipelineId) {
        runPipelineFromQueue(pipelineId)
    }

    /**
     * WORKER 3.
     * Listener for run pipeline.
     * @param pipelineId Running pipeline id.
     */
    @RabbitListener(queues = "pipeline-run-queue")
    void runPipelineFromQueueWorker3(String pipelineId) {
        runPipelineFromQueue(pipelineId)
    }

    /**
     * WORKER 4.
     * Listener for run pipeline.
     * @param pipelineId Running pipeline id.
     */
    @RabbitListener(queues = "pipeline-run-queue")
    void runPipelineFromQueueWorker4(String pipelineId) {
        runPipelineFromQueue(pipelineId)
    }

    /**
     * Runs hierarchy dependents pipelines.
     * @param hierarchy
     */
    @RabbitListener(queues = "pipeline-run-hierarchy")
    void runDependentsHierarchyPipelines(List<String> hierarchy) {
        String pipelineId = hierarchy.remove(0)
        Pipeline pipeline = runByPipelineId(pipelineId)
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
        Pipeline pipeline = runByPipelineId(pipelineId)
        messagingTemplate.convertAndSend("/pipeline/change", pipeline)
    }

    /**
     * Runs pipeline process by pipeline id.
     * @param id
     */
    @RequestMapping("/run/{id}")
    void addToRunQueue(@PathVariable String id) {
        rabbitTemplate.convertAndSend("pipeline-run-queue", id)
    }

    /**
     * Stop pipeline process by pipeline id.
     * @param id
     */
    @RequestMapping("/stop/{id}")
    void stopPipeline(@PathVariable String id) {
        def pipeline = pipelineRepository.findOne(id)

        PipelineProcess pipelineProcess = (PipelineProcess) beanFactory.getSingleton(id)

        pipeline.status = pipelineStatusRepository.findByTitle(PipelineStatuses.STOPPED)
        pipelineRepository.save(pipeline)

        if (pipelineProcess) {
            logger.trace("run stopping pipelineProcess by id: ${id}")
            pipelineProcess.stop()
            pipelineProcess.isStopped = true
            return
        }
        logger.trace("runnning pipelineProcess by id: ${id} not found")
    }

    /**
     * Run pipeline.
     * @param id
     */
    Pipeline runByPipelineId(String id) {
        def startedPipelineProcess = beanFactory.getSingleton(id)

        Pipeline pipelineEntity = pipelineRepository.findOne(id)
        if (startedPipelineProcess) {
            if (!pipelineEntity.status.title.equals('running')) {
                stopPipeline(pipelineEntity.id)
            }
            return pipelineEntity
        }
        pipelineEntity.lastStartedOn = new Date()
        PipelineTask pipelineTask = new PipelineTask(pipeline: pipelineEntity)
        pipelineTask.startOn = new Date()
        pipelineTaskRepository.save(pipelineTask)

        try {
            pipelineEntity.status = pipelineStatusRepository.findByTitle('running')
            pipelineRepository.save(pipelineEntity)
            messagingTemplate.convertAndSend("/pipeline/change", pipelineEntity)

            PipelineProcess pipelineProcess = getPipelineProcess(pipelineEntity, null, pipelineTask)

            // register pipeline process in global singleton bean
            beanFactory.registerSingleton(pipelineEntity.id, pipelineProcess)

            AbstractStore store = pipelineProcess.getStore()

            PipelineStoreObserver storeObserver = new PipelineStoreObserver(store, messagingTemplate, pipelineTask)
            store.addObserver(storeObserver)

            pipelineProcess.run()

            def dataParsed = store.getData()
            pipelineTask.data = dataParsed
            String status = pipelineProcess.isStopped ? PipelineStatuses.STOPPED : PipelineStatuses.COMPLETED
            pipelineEntity.status = pipelineStatusRepository.findByTitle(status)
            pipelineEntity.lastParsedLinesCount = dataParsed.size()
            uploadToElastic(dataParsed as List<HashMap<String, String>>, pipelineEntity.id, pipelineTask.id)
        } catch (all) {
            logger.error(all.printStackTrace())
            pipelineTask.error = "${all.getMessage()}. ${all.printStackTrace()}"
            pipelineEntity.status = pipelineStatusRepository.findByTitle('error')
        }

        PipelineProcess pipelineProcessed = (PipelineProcess) beanFactory.getSingleton(id)
        if (pipelineProcessed) {
            beanFactory.destroySingleton(id)
        }

        pipelineTask.pipeline = pipelineEntity
        pipelineEntity.lastCompletedOn = new Date()
        pipelineTask.endOn = new Date()
        pipelineTaskRepository.save(pipelineTask)
        pipelineRepository.save(pipelineEntity)

        return pipelineEntity
    }

    /**
     * Runs pipeline process by pipeline id.
     * @param id
     * @throws UnknownHostException
     */
    @RequestMapping("/run-child/{childId}/{parentTaskId}")
    void runChild(@PathVariable String childId, @PathVariable String parentTaskId) throws UnknownHostException {
        PipelineTask pipelineParentTask = pipelineTaskRepository.findOne(parentTaskId)
        Pipeline pipelineEntityChild = pipelineRepository.findOne(childId)
        Store parentStore = runPipeline(pipelineEntityChild, pipelineParentTask.data as List<HashMap<String, String>>)
        runPipeline(pipelineEntityChild, parentStore.getData())
    }

    /**
     * Runs pipeline process by pipeline id.
     * @param id
     * @throws UnknownHostException
     */
    @RequestMapping("/run-in-sequence/{childId}/{parentId}")
    void runInSequence(@PathVariable String childId, @PathVariable String parentId) throws UnknownHostException {
        Pipeline pipelineEntityParent = pipelineRepository.findOne(parentId)
        Pipeline pipelineEntityChild = pipelineRepository.findOne(childId)

        Store parentStore = runPipeline(pipelineEntityParent, null)
        runPipeline(pipelineEntityChild, parentStore.getData())
    }

    /**
     *
     * @param pipelineEntity
     * @param runtimeData
     * @return
     */
    protected Store runPipeline(Pipeline pipelineEntity, List<HashMap<String, String>> runtimeData) {
        PipelineTask pipelineTaskParent = new PipelineTask(pipeline: pipelineEntity)
        pipelineTaskParent.startOn = new Date()
        pipelineTaskRepository.save(pipelineTaskParent)

        AbstractStore store
        try {
            PipelineProcess pipelineProcess = getPipelineProcess(pipelineEntity, runtimeData, pipelineTaskParent)
            store = pipelineProcess.getStore()
            PipelineStoreObserver storeObserver = new PipelineStoreObserver(store, messagingTemplate, pipelineTaskParent)
            store.addObserver(storeObserver)
            pipelineProcess.run()
            def dataParsed = store.getData()
            pipelineTaskParent.data = dataParsed
            pipelineEntity.lastParsedLinesCount = dataParsed.size()
            pipelineRepository.save(pipelineEntity)
            uploadToElastic(dataParsed as List<HashMap<String, String>>, pipelineEntity.id, pipelineTaskParent.id)
        } catch (all) {
            println(all.printStackTrace())
            pipelineTaskParent.error = all.printStackTrace()
        }

        pipelineTaskParent.endOn = new Date()
        pipelineTaskRepository.save(pipelineTaskParent)
        return store
    }

    /**
     * Gets pipeline process executor.
     * @param pipelineEntity
     * @return
     */
    private PipelineProcess getPipelineProcess(Pipeline pipelineEntity,
           List<HashMap<String, String>> runtimeData, PipelineTask task) {
        PipelineBuilder pipelineBuilder = new PipelineBuilder()
        Browser browser = getPipelineBrowser(pipelineEntity)

        String tmpFolder = "${TEMP_DIRECTORY}/${task.id}"
        Environment environment = new Environment(
                runningTmpDir: tmpFolder,
                isTakeScreenshot: pipelineEntity.isTakeScreenshot,
                pipelineId: task.id,
                isDebug: pipelineEntity.isDebugMode
        )

        if (pipelineEntity.jsonCommandsPath) {
            pipelineBuilder.setPipelineJsonFilePath(pipelineEntity.jsonCommandsPath)
        }
        if (pipelineEntity.jsonCommands) {
            pipelineBuilder.setPipelineJson(pipelineEntity.jsonCommands)
        }
        if (runtimeData) {
            pipelineBuilder.setRuntimePushedData(runtimeData)
        }
        PipelineProcess pipelineProcess = pipelineBuilder
                .setBrowser(browser)
                .setEnvironment(environment)
                .build()
        return pipelineProcess
    }

    /**
     * Gets pipeline browser.
     * @param pipelineEntity
     * @return
     */
    private Browser getPipelineBrowser(Pipeline pipelineEntity) {
        def factory = new BrowserFactory()
        def config = [enableImage: false]
        if (pipelineEntity.browserAddress) {
            config = [browserAddress: pipelineEntity.browserAddress, enableImage: false]
        }
        if (pipelineEntity && pipelineEntity.browser) {
            return factory.createFromString(pipelineEntity.browser, config)
        }
        return factory.createFromClass(DEFAULT_BROWSER, config)
    }

    private void uploadToElastic(List<HashMap<String, String>> list, String index, String type) {
        elasticSearchService.bulk(list, index, type)
    }

    /**
     * Gets last parsed data by pipeline id.
     * @param pipelineId
     * @return Last parsed data by pipeline id.
     */
    @RequestMapping("/data/{pipelineId}")
    List<HashMap> getData(@PathVariable String pipelineId) {
        PipelineTask pipelineTask = pipelineTaskRepository.findOneByPipelineOrderByEndOnDesc(pipelineId)
        return pipelineTask.data
    }

    /**
     * Gets last parsed data by pipeline id.
     * @param pipelineId
     * @return Last parsed data by pipeline id.
     */
    @RequestMapping("/data/csv/{pipelineId}")
    List<HashMap> getCsvData(@PathVariable String pipelineId, HttpServletResponse response) {
        String responseData = ''
        PipelineTask pipelineTask = pipelineTaskRepository.findOneByPipelineOrderByEndOnDesc(pipelineId)
        List<HashMap> list = pipelineTask.data as List<HashMap>
        HashSet columns = new HashSet()
        list.each { map ->
            map.each {k, v -> columns.add(k)}
        }
        def encode = { e -> e == null ? '' : e instanceof String ? /"$e"/ : "$e" }
        responseData += columns.collect { c -> encode( c ) }.join( ',' )
        responseData += '\n'
        responseData += list.collect { row ->
            columns.collect { colName -> encode( row[ colName ] ) }.join( ',' )
        }.join( '\n' )
        response.setContentType("text/csv; charset=utf-8")
        response.setHeader("Content-disposition", "attachment;filename=${pipelineId}.csv")
        response.getWriter().print(responseData)
    }
}
