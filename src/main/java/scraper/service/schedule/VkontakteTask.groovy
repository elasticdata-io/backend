package scraper.service.schedule

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import scraper.core.browser.Browser
import scraper.core.browser.BrowserFactory
import scraper.core.browser.BrowserProvider
import scraper.core.browser.provider.Phantom
import scraper.core.pipeline.PipelineBuilder
import scraper.core.pipeline.PipelineProcess
import scraper.core.pipeline.data.Store
import scraper.service.model.Pipeline
import scraper.service.model.PipelineTask
import scraper.service.repository.PipelineRepository
import scraper.service.repository.PipelineTaskRepository

@Component
class VkontakteTask {

    private static final Logger log = LoggerFactory.getLogger(VkontakteTask.class);

    private static Class DEFAULT_BROWSER = Phantom.class;

    @Autowired
    PipelineRepository pipelineRepository;

    @Autowired
    PipelineTaskRepository pipelineTaskRepository;

//    @Scheduled(cron="* */20 * * * *")
    public void parseMessages() {
        log.info('run parseMessages');
        String id = "5928b48fd95579a675bbe75f";
        Pipeline pipelineEntity = pipelineRepository.findOne(id);

        PipelineTask pipelineTask = new PipelineTask();
        pipelineTask.startOn = new Date();

        try {
            PipelineProcess pipelineProcess = getPipelineProcess(pipelineEntity, null);
            pipelineProcess.run();
            Store store = pipelineProcess.getStore();
            pipelineTask.data = store.getData();
            pipelineTask.pipeline = pipelineEntity;
        } catch (all) {
            println(all.printStackTrace());
            pipelineTask.error = all.printStackTrace();
        }

        pipelineTask.endOn = new Date();
        pipelineTaskRepository.save(pipelineTask);
    }

    /**
     * Gets pipeline process executor.
     * @param pipelineEntity
     * @return
     */
    private PipelineProcess getPipelineProcess(Pipeline pipelineEntity, List<HashMap<String, String>> runtimeData) {
        PipelineBuilder pipelineBuilder = new PipelineBuilder();
        Browser browser = getPipelineBrowser(pipelineEntity);
        BrowserProvider driverProvider = new BrowserProvider(webDriver: browser.create());
        if (pipelineEntity.jsonCommandsPath) {
            pipelineBuilder.setPipelineJsonFilePath(pipelineEntity.jsonCommandsPath);
        }
        if (pipelineEntity.jsonCommands) {
            pipelineBuilder.setPipelineJson(pipelineEntity.jsonCommands);
        }
        if (runtimeData) {
            pipelineBuilder.setRuntimePushedData(runtimeData);
        }
        PipelineProcess pipelineProcess = pipelineBuilder.setDriverProvider(driverProvider).build();
        return pipelineProcess;
    }

    /**
     * Gets pipeline browser.
     * @param pipelineEntity
     * @return
     */
    private Browser getPipelineBrowser(Pipeline pipelineEntity) {
        def factory = new BrowserFactory();
        if (pipelineEntity && pipelineEntity.browser) {
            return factory.createFromString(pipelineEntity.browser);
        }
        return factory.createFromClass(DEFAULT_BROWSER);
    }
}
