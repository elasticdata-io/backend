package scraper.service.controller

import org.apache.logging.log4j.LogManager
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
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
import scraper.core.pipeline.data.Store
import scraper.service.model.Pipeline
import scraper.service.model.PipelineTask
import scraper.service.repository.PipelineRepository
import scraper.service.repository.PipelineStatusRepository
import scraper.service.repository.PipelineTaskRepository

@RestController
@RequestMapping("/api/pipeline")
class PipelineController {

    private static String TEMP_DIRECTORY = '/tmp/scraper-service'
    private static Class DEFAULT_BROWSER = Phantom.class
    public static final String  = System.getProperty('java.io.tmpdir')

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
     * Build, created instance and runs pipeline by database id.
     * @param pipelineId Running pipeline id.
     */
    private void runPipelineFromQueue(String pipelineId) {
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
        def configurableContext = ((ConfigurableApplicationContext) applicationContext)
        def beanFactory = configurableContext.getBeanFactory()
        PipelineProcess pipelineProcess = (PipelineProcess) beanFactory.getSingleton(id)
        def logger = LogManager.getRootLogger()
        if (pipelineProcess) {
            logger.info("run stopping pipelineProcess by id: ${id}")
            pipelineProcess.stop()
            beanFactory.destroyScopedBean(id)
            return
        }
        logger.info("runnning pipelineProcess by id: ${id} not found")
    }

    /**
     * Run pipeline.
     * @param id
     */
    Pipeline runByPipelineId(String id) {
        def configurableContext = ((ConfigurableApplicationContext) applicationContext)
        def beanFactory = configurableContext.getBeanFactory()
        def startedPipelineProcess = beanFactory.getSingleton(id)

        Pipeline pipelineEntity = pipelineRepository.findOne(id)
        if (!startedPipelineProcess) {
            return pipelineEntity
        }
        pipelineEntity.lastStartedOn = new Date()
        PipelineTask pipelineTask = new PipelineTask()
        pipelineTask.startOn = new Date()
        pipelineTaskRepository.save(pipelineTask)

        try {
            pipelineEntity.status = pipelineStatusRepository.findByTitle('running')
            pipelineRepository.save(pipelineEntity)
            messagingTemplate.convertAndSend("/pipeline/change", pipelineEntity)

            PipelineProcess pipelineProcess = getPipelineProcess(pipelineEntity, null, pipelineTask)

            // register pipeline process in global singleton bean
            beanFactory.registerSingleton(pipelineEntity.id, pipelineProcess)

            pipelineProcess.run()
            Store store = pipelineProcess.getStore()
            pipelineTask.data = store.getData()
            pipelineEntity.status = pipelineStatusRepository.findByTitle('completed')
        } catch (all) {
            println(all.printStackTrace())
            pipelineTask.error = "${all.getMessage()}. ${all.printStackTrace()}"
            pipelineEntity.status = pipelineStatusRepository.findByTitle('error')
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

        Store prentStore = runPipeline(pipelineEntityParent, null)
        runPipeline(pipelineEntityChild, prentStore.getData())
    }

    /**
     * Gets last parsed data by pipeline id.
     * @param pipelineId
     * @return Last parsed data by pipeline id.
     */
    @RequestMapping("/last-task-data/{pipelineId}")
    List<HashMap> getData(@PathVariable String pipelineId) {
        PipelineTask pipelineTask = pipelineTaskRepository.findOneByPipelineOrderByEndOnDesc(pipelineId)
        return pipelineTask.data
    }

    /**
     *
     * @param pipelineEntity
     * @param runtimeData
     * @return
     */
    protected Store runPipeline(Pipeline pipelineEntity, List<HashMap<String, String>> runtimeData) {
        PipelineTask pipelineTaskParent = new PipelineTask()
        pipelineTaskParent.startOn = new Date()
        pipelineTaskRepository.save(pipelineTaskParent)

        Store store
        try {
            PipelineProcess pipelineProcess = getPipelineProcess(pipelineEntity, runtimeData, pipelineTaskParent)
            pipelineProcess.run()
            store = pipelineProcess.getStore()
            pipelineTaskParent.data = store.getData()
            pipelineTaskParent.pipeline = pipelineEntity
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
                isTakeScreenshot: pipelineEntity.isTakeScreenshot
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
                .setLogger(LogManager.getRootLogger())
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
        def config = []
        if (pipelineEntity.browserAddress) {
            config = [browserAddress: pipelineEntity.browserAddress]
        }
        if (pipelineEntity && pipelineEntity.browser) {
            return factory.createFromString(pipelineEntity.browser, config)
        }
        return factory.createFromClass(DEFAULT_BROWSER, config)
    }
}
