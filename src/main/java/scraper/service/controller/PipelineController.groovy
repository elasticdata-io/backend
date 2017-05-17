package scraper.service.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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

@RestController
@RequestMapping("/api/pipeline")
class PipelineController {

    private static Class DEFAULT_BROWSER = Phantom.class;

    @Autowired
    PipelineRepository pipelineRepository;

    @Autowired
    PipelineTaskRepository pipelineTaskRepository;

    /**
     * Runs pipeline process by pipeline id.
     * @param id
     * @throws UnknownHostException
     */
    @RequestMapping("/run/{id}")
    public void run(@PathVariable String id) throws UnknownHostException {
        Pipeline pipelineEntity = pipelineRepository.findOne(id);

        PipelineTask pipelineTask = new PipelineTask();
        pipelineTask.startOn = new Date();

        try {
            PipelineProcess pipelineProcess = getPipelineProcess(pipelineEntity);
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
    private PipelineProcess getPipelineProcess(Pipeline pipelineEntity) {
        PipelineBuilder pipelineBuilder = new PipelineBuilder();
        Browser browser = getPipelineBrowser(pipelineEntity);
        BrowserProvider driverProvider = new BrowserProvider(webDriver: browser.create());
        if (pipelineEntity.jsonCommandsPath) {
            pipelineBuilder.setPipelineJsonFilePath(pipelineEntity.jsonCommandsPath);
        }
        if (pipelineEntity.jsonCommands) {
            pipelineBuilder.setPipelineJson(pipelineEntity.jsonCommands);
        }
        return pipelineBuilder.setDriverProvider(driverProvider).build();
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
