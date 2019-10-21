package scraper.service.service

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Service
import scraper.core.browser.Browser
import scraper.core.browser.BrowserFactory
import scraper.core.browser.BrowserProvider
import scraper.core.browser.provider.Chrome
import scraper.core.pipeline.Environment
import scraper.core.pipeline.PipelineBuilder
import scraper.core.pipeline.PipelineProcess
import scraper.core.pipeline.data.ObservableStore
import scraper.service.amqp.producer.PipelineProducer
import scraper.service.amqp.producer.PipelineTaskProducer
import scraper.service.constants.PipelineStatuses
import scraper.service.controller.listener.PipelineStateCommandsObserver
import scraper.service.controller.listener.PipelineStoreObserver
import scraper.service.dto.mapper.PipelineMapper
import scraper.service.elastic.ElasticSearchService
import scraper.service.model.Pipeline
import scraper.service.model.PipelineTask
import scraper.service.repository.PipelineRepository
import scraper.service.repository.PipelineStatusRepository
import scraper.service.repository.PipelineTaskRepository
import scraper.service.proxy.ProxyAssigner
import scraper.service.ws.PipelineWebsockerProducer

import javax.annotation.PostConstruct

@Service
class PipelineService {

    private Logger logger = LogManager.getRootLogger()

    private DefaultListableBeanFactory beanFactory

    private Class DEFAULT_BROWSER = Chrome.class

    @Value('${static.runDirectory}')
    private String RUN_DIRECTORY

    @Value('${selenium.default.address}')
    String SELENIUM_DEFAULT_ADDRESS

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

    @Autowired
    PipelineTaskProducer pipelineTaskProducer

    @Autowired
    PipelineProducer pipelineProducer

    @Autowired
    ProxyAssigner proxyAssigner

    @PostConstruct
    void initialise() {
        def configurableContext = applicationContext as ConfigurableApplicationContext
        beanFactory = configurableContext.getBeanFactory() as DefaultListableBeanFactory
    }

    @Autowired
    private PipelineStructureService pipelineStructure

    @Autowired
    private PipelineWebsockerProducer pipelineWebsockerProducer

    void run(String pipelineId) {
        Pipeline pipeline = findById(pipelineId)
        runPipeline(pipeline)
    }

    void notifyChangePipeline(Pipeline pipeline) {
        def pipelineDto = PipelineMapper.toPipelineDto(pipeline)
        pipelineWebsockerProducer.change(pipelineDto)
    }

    void stop(String pipelineId) {
        Pipeline pipeline = findById(pipelineId)
        PipelineProcess pipelineProcess = (PipelineProcess) beanFactory.getSingleton(pipelineId)
        if (pipelineProcess) {
            logger.trace("run stopping pipelineProcess by pipelineId: ${pipelineId}")
            pipelineProcess.stop()
        } else {
            logger.trace("runnning pipelineProcess by pipelineId: ${pipelineId} not found")
        }
        pipeline.status = pipelineStatusRepository.findByTitle(PipelineStatuses.STOPPED)
        pipelineRepository.save(pipeline)
        notifyChangePipeline(pipeline)
    }

    private PipelineProcess getPipelineProcessBean(Pipeline pipeline) {
        return getPipelineProcessBeanById(pipeline.id)
    }

    PipelineProcess getPipelineProcessBeanById(String pipelineId) {
        return beanFactory.getSingleton(pipelineId) as PipelineProcess
    }

    private PipelineTask beforeRun(Pipeline pipeline) {
        PipelineTask pipelineTask = new PipelineTask(pipeline: pipeline)
        pipelineTask.startOn = new Date()
        pipelineTaskRepository.save(pipelineTask)
        return pipelineTask
    }

    private void afterRegisterPipelineProcessBean(String pipelineId) {
        Pipeline pipeline = findById(pipelineId)
        pipeline.status = pipelineStatusRepository.findByTitle(PipelineStatuses.RUNNING)
        pipeline.lastStartedOn = new Date()
        pipelineRepository.save(pipeline)
        notifyChangePipeline(pipeline)
    }

    private void afterRun(PipelineTask pipelineTask, PipelineProcess pipelineProcess) {
        ObservableStore store = pipelineProcess ? pipelineProcess.getStore() : null
        def dataList = store ? store.getData() : []
        pipelineTask.data = dataList
        pipelineTask.endOn = new Date()

        String status = PipelineStatuses.COMPLETED
        if (pipelineProcess?.hasBeenStopped) {
            status = PipelineStatuses.STOPPED
        }
        if (!pipelineProcess
                || pipelineProcess?.hasErrors
                || pipelineTask.pipeline.status.title == PipelineStatuses.ERROR) {
            status = PipelineStatuses.ERROR
        }
        Pipeline pipeline = findById(pipelineTask.pipeline.id)
        pipeline.status = pipelineStatusRepository.findByTitle(status)
        pipeline.lastCompletedOn = new Date()
        pipeline.tasksTotal = (pipeline.tasksTotal ?: 0) + 1
        pipeline.parseRowsCount = dataList ? dataList.size() : 0

        pipelineTaskRepository.save(pipelineTask)
        pipelineRepository.save(pipeline)
        destroyPipelineProcess(pipeline)
        notifyChangePipeline(pipeline)

        pipelineTaskProducer.taskFinish(pipelineTask.id)
        pipelineProducer.finish(pipeline.id)

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
        uploadToElastic(list as List<HashMap<String, Object>>, pipelineTask.pipeline.id, pipelineTask.id)
    }

    private void uploadToElastic(List<HashMap<String, Object>> list, String index, String type) {
        elasticSearchService.bulk(list, index, type)
    }

    private PipelineProcess createPipelineProcess(Pipeline pipeline, List runtimeData,
        PipelineTask task) {
        PipelineBuilder pipelineBuilder = new PipelineBuilder()

        String tmpFolder = "${RUN_DIRECTORY}/${task.id}"
        Environment environment = new Environment(
                runningTmpDir: tmpFolder,
                isTakeScreenshot: pipeline.isTakeScreenshot,
                pipelineId: task.id,
                isDebug: pipeline.isDebugMode
        )
        Browser browser = getPipelineBrowser(pipeline, environment)
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
    private Browser getPipelineBrowser(Pipeline pipeline, Environment environment) {
        def factory = new BrowserFactory()
        def config = [
                enableImage: true,
                browserAddress: pipeline.browserAddress ?: SELENIUM_DEFAULT_ADDRESS
        ]
        if (pipeline.isDebugMode) {
            config += [isDebugMode: pipeline.isDebugMode]
        }
        if (pipeline.needProxy) {
            String proxy = proxyAssigner.getProxy()
            if (proxy) {
                config += [proxy: proxy]
            }
        }
        return factory.createFromClass(DEFAULT_BROWSER, config)
    }

    /**
     * Run pipeline.
     * @param pipeline
     */
    private Pipeline runPipeline(Pipeline pipeline) {
        PipelineProcess runningPipelineProcess = getPipelineProcessBean(pipeline)
        if (runningPipelineProcess) {
            return pipeline
        }
        PipelineTask pipelineTask = beforeRun(pipeline)
        PipelineProcess pipelineProcess = null
        try {
            pipelineProcess = createPipelineProcess(pipeline, null, pipelineTask)

            beanFactory.registerSingleton(pipeline.id, pipelineProcess)
            afterRegisterPipelineProcessBean(pipeline.id)

            bindStoreObserver(pipelineProcess, pipelineTask)
            bindCommandObserver(pipelineProcess, pipelineTask)

            pipelineProcess.run()
        } catch (Error error) {
            logger.error(error)
            pipelineTask.error = "${error.getClass()}"
            def errorStatus = pipelineStatusRepository.findByTitle(PipelineStatuses.ERROR)
            pipelineTask.pipeline.status = errorStatus
            pipelineTaskRepository.save(pipelineTask)
        } catch (all) {
            logger.error(all)
            pipelineTask.error = "${all.getMessage()}. ${all.printStackTrace()}"
            def errorStatus = pipelineStatusRepository.findByTitle(PipelineStatuses.ERROR)
            pipelineTask.pipeline.status = errorStatus
            pipelineTaskRepository.save(pipelineTask)
        }

        try {
            afterRun(pipelineTask, pipelineProcess)
        } catch (all) {
            logger.error(all)
            def errorStatus = pipelineStatusRepository.findByTitle(PipelineStatuses.ERROR)
            pipeline.status = errorStatus
            pipelineRepository.save(pipeline)
        }
        return findById(pipeline.id)
    }

    private void bindStoreObserver(PipelineProcess pipelineProcess, PipelineTask pipelineTask) {
        ObservableStore store = pipelineProcess.getStore()
        def observer = new PipelineStoreObserver(pipelineWebsockerProducer, pipelineTask)
        store.subscribe(observer)
    }

    private void bindCommandObserver(PipelineProcess pipelineProcess, PipelineTask pipelineTask) {
        BrowserProvider browserProvider = pipelineProcess.getBrowserProvider()
        def observer = new PipelineStateCommandsObserver(pipelineWebsockerProducer, pipelineTask)
        browserProvider.subscribe(observer)
    }

    Pipeline findById(String id) {
        Optional<Pipeline> pipeline = pipelineRepository.findById(id)
        return pipeline.present ? pipeline.get() : null
    }

    void save(Pipeline pipeline) {
        pipelineRepository.save(pipeline)
    }

    Pipeline findByDependenciesAndStatusWaiting(String dependencyPipelineId) {
        def waitingStatus = pipelineStatusRepository.findByTitle(PipelineStatuses.WAIT_OTHER_PIPELINE)
        return pipelineRepository.findByDependenciesAndStatus(dependencyPipelineId, waitingStatus.id)
    }
}
