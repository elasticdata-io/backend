package scraper.service.pipeline

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import scraper.core.browser.Browser
import scraper.core.browser.BrowserFactory
import scraper.core.browser.BrowserProvider
import scraper.core.browser.provider.Chrome
import scraper.core.pipeline.Environment
import scraper.core.pipeline.PipelineBuilder
import scraper.core.pipeline.PipelineProcess
import scraper.core.pipeline.data.AbstractStore
import scraper.service.constants.PipelineStatuses
import scraper.service.controller.listener.PipelineBrowserProviderObserver
import scraper.service.controller.listener.PipelineStoreObserver
import scraper.service.elastic.ElasticSearchService
import scraper.service.model.Pipeline
import scraper.service.model.PipelineTask
import scraper.service.repository.PipelineRepository
import scraper.service.repository.PipelineStatusRepository
import scraper.service.repository.PipelineTaskRepository
import scraper.service.util.PipelineStructure

import javax.annotation.PostConstruct

@Service
class PipelineService {

    private Logger logger = LogManager.getRootLogger()

    private DefaultListableBeanFactory beanFactory

    private String RUN_DIRECTORY = '/tmp/scraper-service'
    private Class DEFAULT_BROWSER = Chrome.class

    @Autowired
    private ApplicationContext applicationContext

    @Autowired
    private PipelineRepository pipelineRepository

    @Autowired
    private PipelineTaskRepository pipelineTaskRepository

    @Autowired
    private PipelineStatusRepository pipelineStatusRepository

    @Autowired
    private ElasticSearchService elasticSearchService

    @PostConstruct
    void initialise() {
        def configurableContext = applicationContext as ConfigurableApplicationContext
        beanFactory = configurableContext.getBeanFactory() as DefaultListableBeanFactory
    }

    @Autowired
    private PipelineStructure pipelineStructure

    @Autowired
    private SimpMessagingTemplate messagingTemplate


    Pipeline run(String pipelineId) {
        Pipeline pipeline = pipelineRepository.findOne(pipelineId)
        Pipeline resultPipeline = runPipeline(pipeline)
        return resultPipeline
    }

    void stop(String pipelineId) {
        Pipeline pipeline = pipelineRepository.findOne(pipelineId)
        PipelineProcess pipelineProcess = (PipelineProcess) beanFactory.getSingleton(pipelineId)

        pipeline.status = pipelineStatusRepository.findByTitle(PipelineStatuses.STOPPED)
        pipelineRepository.save(pipeline)

        if (pipelineProcess) {
            logger.trace("run stopping pipelineProcess by id: ${pipelineId}")
            pipelineProcess.stop()
            pipelineProcess.isStopped = true
            return
        }
        logger.trace("runnning pipelineProcess by id: ${pipelineId} not found")

    }

    private PipelineProcess getPipelineBean(Pipeline pipeline) {
        return beanFactory.getSingleton(pipeline.id)
    }

    private PipelineTask beforeRun(Pipeline pipeline) {
        pipeline.lastStartedOn = new Date()
        PipelineTask pipelineTask = new PipelineTask(pipeline: pipeline)
        pipelineTask.startOn = new Date()
        pipelineTaskRepository.save(pipelineTask)

        pipeline.status = pipelineStatusRepository.findByTitle(PipelineStatuses.RUNNING)
        pipelineRepository.save(pipeline)
        return pipelineTask
    }

    private void afterRun(PipelineTask pipelineTask, PipelineProcess pipelineProcess = null) {
        AbstractStore store = pipelineProcess ? pipelineProcess.getStore() : null
        def dataList = store ? store.getData() : null
        pipelineTask.data = dataList
        pipelineTask.endOn = new Date()

        String status = null
        if (pipelineProcess && pipelineProcess.isStopped) {
            status = PipelineStatuses.STOPPED
        } else {
            status = pipelineProcess ? PipelineStatuses.COMPLETED : PipelineStatuses.ERROR
        }
        Pipeline pipeline = pipelineRepository.findOne(pipelineTask.pipeline.id)
        pipeline.status = pipelineStatusRepository.findByTitle(status)
        pipeline.lastCompletedOn = new Date()
        pipeline.parseRowsCount = dataList ? dataList.size() : 0

        pipelineTaskRepository.save(pipelineTask)
        pipelineRepository.save(pipeline)

        destroyPipelineProcess(pipeline)
        if (dataList) {
            uploadDataToElastic(dataList as List<HashMap>, pipelineTask)
        }
    }

    private destroyPipelineProcess(Pipeline pipeline) {
        PipelineProcess pipelineProcessed = beanFactory.getSingleton(pipeline.id) as PipelineProcess
        if (pipelineProcessed) {
            beanFactory.destroySingleton(pipeline.id)
        }
    }

    private void uploadDataToElastic(List<HashMap> list, PipelineTask pipelineTask) {
        if (!list) {
            return
        }
        uploadToElastic(list as List<HashMap<String, String>>, pipelineTask.pipeline.id, pipelineTask.id)
    }

    private void uploadToElastic(List<HashMap<String, String>> list, String index, String type) {
        elasticSearchService.bulk(list, index, type)
    }

    private PipelineProcess createPipelineProcess(Pipeline pipeline, List runtimeData,
        PipelineTask task) {
        PipelineBuilder pipelineBuilder = new PipelineBuilder()
        Browser browser = getPipelineBrowser(pipeline)

        String tmpFolder = "${RUN_DIRECTORY}/${task.id}"
        Environment environment = new Environment(
                runningTmpDir: tmpFolder,
                isTakeScreenshot: pipeline.isTakeScreenshot,
                pipelineId: task.id,
                isDebug: pipeline.isDebugMode
        )

        if (pipeline.jsonCommandsPath) {
            pipelineBuilder.setPipelineJsonFilePath(pipeline.jsonCommandsPath)
        }
        if (pipeline.jsonCommands) {
            pipelineBuilder.setPipelineJson(pipeline.jsonCommands)
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
     * @param pipeline
     * @return
     */
    private Browser getPipelineBrowser(Pipeline pipeline) {
        def factory = new BrowserFactory()
        def config = [enableImage: false]
        if (pipeline.browserAddress) {
            config += [browserAddress: pipeline.browserAddress]
        }
        if (pipeline.isDebugMode) {
            config += [isDebugMode: pipeline.isDebugMode]
        }
        return factory.createFromClass(DEFAULT_BROWSER, config)
    }

    /**
     * Run pipeline.
     * @param pipeline
     */
    private Pipeline runPipeline(Pipeline pipeline) {
        PipelineProcess runningPipelineProcess = getPipelineBean(pipeline)
        if (runningPipelineProcess) {
            if (!pipeline.status.title.equals(PipelineStatuses.RUNNING)) {
                stop(pipeline)
            }
            return pipeline
        }

        PipelineTask pipelineTask = beforeRun(pipeline)

        PipelineProcess pipelineProcess = null

        try {
            // TODO send message only watched user
            messagingTemplate.convertAndSend("/pipeline/change", pipeline)

            pipelineProcess = createPipelineProcess(pipeline, null, pipelineTask)

            beanFactory.registerSingleton(pipeline.id, pipelineProcess)
            bindStoreObserver(pipelineProcess, pipelineTask)
            bindCommandObserver(pipelineProcess, pipelineTask)

            pipelineProcess.run()

            afterRun(pipelineTask, pipelineProcess)
        } catch (all) {
            logger.error(all.printStackTrace())
            pipelineTask.error = "${all.getMessage()}. ${all.printStackTrace()}"
            pipelineTask.pipeline.status = pipelineStatusRepository.findByTitle('error')
            afterRun(pipelineTask, pipelineProcess)
        }

        return pipelineRepository.findOne(pipeline.id)
    }

    private void bindStoreObserver(PipelineProcess pipelineProcess, PipelineTask pipelineTask) {
        AbstractStore store = pipelineProcess.getStore()
        PipelineStoreObserver storeObserver = new PipelineStoreObserver(store, messagingTemplate, pipelineTask)
        store.addObserver(storeObserver)
    }

    private void bindCommandObserver(PipelineProcess pipelineProcess, PipelineTask pipelineTask) {
        BrowserProvider browserProvider = pipelineProcess.getBrowserProvider()
        def observer = new PipelineBrowserProviderObserver(browserProvider, messagingTemplate, pipelineTask)
        browserProvider.addObserver(observer)
    }

}
