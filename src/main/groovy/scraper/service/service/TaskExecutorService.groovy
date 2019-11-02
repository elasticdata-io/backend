package scraper.service.service

import groovy.io.FileType
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
import scraper.core.pipeline.data.FileStore
import scraper.core.pipeline.data.storage.FileStoreProvider
import scraper.core.pipeline.data.ObservableStore
import scraper.service.amqp.producer.TaskProducer
import scraper.service.constants.PipelineStatuses
import scraper.service.controller.listener.PipelineStateCommandsObserver
import scraper.service.controller.listener.PipelineStoreObserver
import scraper.service.dto.mapper.TaskMapper
import scraper.service.elastic.ElasticSearchService
import scraper.service.model.Pipeline
import scraper.service.model.Task
import scraper.service.proxy.ProxyAssigner
import scraper.service.ws.TaskWebsocketProducer

import javax.annotation.PostConstruct

@Service
class TaskExecutorService {

    private Class DEFAULT_BROWSER = Chrome.class
    private DefaultListableBeanFactory beanFactory
    private Logger logger = LogManager.getRootLogger()

    @Autowired
    private ApplicationContext applicationContext

    @PostConstruct
    void initialise() {
        def configurableContext = applicationContext as ConfigurableApplicationContext
        beanFactory = configurableContext.getBeanFactory() as DefaultListableBeanFactory
    }

    @Value('${static.runDirectory}')
    private String RUN_DIRECTORY

    @Value('${selenium.default.address}')
    String SELENIUM_DEFAULT_ADDRESS

    @Autowired
    ProxyAssigner proxyAssigner

    @Autowired
    PipelineService pipelineService

    @Autowired
    TaskService taskService

    @Autowired
    TaskProducer taskProducer

    @Autowired
    TaskWebsocketProducer taskWebsocketProducer

    @Autowired
    private ElasticSearchService elasticSearchService

    @Autowired
    private FileStoreProvider fileStoreProvider

    /**
     *
     * @param task
     * @return
     */
    Task run(Task task) {
        if (!task?.pipelineId) {
            throw new Exception("pipelineId not found")
        }
        Pipeline pipeline = pipelineService.findById(task.pipelineId)
        if (!pipeline) {
            throw new Exception("pipeline with id ${task.pipelineId} not found")
        }
        logger.info("run task ${task.id}")
        runPipeline(task, pipeline)
    }

    /**
     *
     * @param task
     * @return
     */
    Task stop(Task task) {
        PipelineProcess pipelineProcess = (PipelineProcess) beanFactory.getSingleton(task.id)
        if (pipelineProcess) {
            logger.trace("run stopping pipelineProcess by task.id: ${task.id}")
            pipelineProcess.stop()
        } else {
            logger.trace("runnning pipelineProcess by task.id: ${task.id} not found")
        }
        task.status = PipelineStatuses.STOPPED
        task.endOnUtc = new Date()
        taskService.update(task)
    }

    private Task runPipeline(Task task, Pipeline pipeline) {
        PipelineProcess runningPipelineProcess = getPipelineProcessBean(task)
        if (runningPipelineProcess) {
            logger.info("runningPipelineProcess has been started for task ${task.id}")
            return task
        }
        beforeRun(task)
        PipelineProcess pipelineProcess = null
        try {
            pipelineProcess = createPipelineProcess(pipeline, null, task)
            beanFactory.registerSingleton(task.id, pipelineProcess)
            afterRegisterPipelineProcessBean(task)
            bindStoreObserver(pipelineProcess, task)
            bindCommandObserver(pipelineProcess, task)
            pipelineProcess.run()
        } catch (Error error) {
            logger.error(error)
            task.failureReason = "${error.getClass()}"
            task.status = PipelineStatuses.ERROR
            taskService.update(task)
        } catch (all) {
            logger.error(all)
            task.failureReason = "${all.getMessage()}. ${all.printStackTrace()}"
            task.status = PipelineStatuses.ERROR
            taskService.update(task)
        }

        try {
            afterRun(task, pipelineProcess)
        } catch (all) {
            logger.error(all)
            task.status = PipelineStatuses.ERROR
            taskService.update(task)
        }
        logger.info("finished task ${task.id}")
        return task
    }

    private PipelineProcess getPipelineProcessBean(Task task) {
        return beanFactory.getSingleton(task.id) as PipelineProcess
    }

    private Task beforeRun(Task task) {
        return task
    }
    private void afterRun(Task task, PipelineProcess pipelineProcess) {
        ObservableStore store = pipelineProcess ? pipelineProcess.getStore() : null
        List docs = []
        if (store) {
            FileStore fileStore = store as FileStore
            task.docsUrl = fileStore.docsUrl
            docs = fileStore.getData()
        }
        task.endOnUtc = new Date()

        moveLocalFilesToStorage(pipelineProcess)

        String status = PipelineStatuses.COMPLETED
        if (pipelineProcess?.hasBeenStopped) {
            status = PipelineStatuses.STOPPED
        }
        if (!pipelineProcess
                || pipelineProcess?.hasErrors
                || task.status == PipelineStatuses.ERROR) {
            status = PipelineStatuses.ERROR
        }
        task.status = status
        taskService.update(task)
        destroyPipelineProcess(task)
        taskProducer.taskFinish(task.id)
        if (docs) {
            uploadDataToElastic(docs as List<HashMap>, task)
        }
        pipelineService.updateFromTask(TaskMapper.toPendingTaskDto(task))
    }

    def moveLocalFilesToStorage(PipelineProcess pipelineProcess) {
        pipelineProcess.environment.runningTmpDir
        def logsDir = new File(pipelineProcess.environment.runningTmpDir)
        if (logsDir.exists()) {
            logsDir.eachFile (FileType.FILES) { file ->
                println file
            }
        }
        def screenshotsDir = new File("${pipelineProcess.environment.runningTmpDir}/screenshots")
        if (screenshotsDir.exists()) {
            screenshotsDir.eachFile (FileType.FILES) { file ->
                println file
            }
        }
    }

    private PipelineProcess createPipelineProcess(Pipeline pipeline, List runtimeData, Task task) {
        PipelineBuilder pipelineBuilder = new PipelineBuilder()
        String tmpFolder = "${RUN_DIRECTORY}/${task.id}"
        Environment environment = new Environment(
                runningTmpDir: tmpFolder,
                isTakeScreenshot: pipeline.isTakeScreenshot,
                pipelineId: task.id,
                isDebug: pipeline.isDebugMode,
                userId: pipeline.user.id,
        )
        Browser browser = getPipelineBrowser(pipeline, environment)
        pipelineBuilder.setPipelineJson(task.commands)
        if (runtimeData) {
            pipelineBuilder.setRuntimePushedData(runtimeData)
        }
        PipelineProcess pipelineProcess = pipelineBuilder
                .setBrowser(browser)
                .setEnvironment(environment)
                .setFileStoreProvider(fileStoreProvider)
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

    private void afterRegisterPipelineProcessBean(Task task) {
        task.status = PipelineStatuses.RUNNING
        taskService.update(task)
    }

    private destroyPipelineProcess(Task task) {
        PipelineProcess pipelineProcessed = beanFactory.getSingleton(task.id) as PipelineProcess
        if (pipelineProcessed) {
            pipelineProcessed.destroy()
            beanFactory.destroySingleton(task.id)
        }
    }

    private void uploadDataToElastic(List<HashMap> list, Task task) {
        if (!list) {
            return
        }
        def docs = list as List<HashMap<String, Object>>;
        uploadToElastic(docs, task.pipelineId, task.id)
    }

    private void uploadToElastic(List<HashMap<String, Object>> list, String index, String type) {
        elasticSearchService.bulk(list, index, type)
    }

    private void bindStoreObserver(PipelineProcess pipelineProcess, Task task) {
        ObservableStore store = pipelineProcess.getStore()
        def observer = new PipelineStoreObserver(taskWebsocketProducer, task)
        store.subscribe(observer)
    }

    private void bindCommandObserver(PipelineProcess pipelineProcess, Task task) {
        BrowserProvider browserProvider = pipelineProcess.getBrowserProvider()
        def observer = new PipelineStateCommandsObserver(taskWebsocketProducer, task)
        browserProvider.subscribe(observer)
    }

}
