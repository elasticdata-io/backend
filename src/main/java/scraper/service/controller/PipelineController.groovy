package scraper.service.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import scraper.core.browser.Browser
import scraper.core.browser.BrowserFactory
import scraper.core.browser.BrowserProvider
import scraper.core.browser.provider.Phantom
import scraper.core.pipeline.PipelineBuilder
import scraper.core.pipeline.PipelineProcess
import scraper.core.pipeline.data.Store
import scraper.service.controller.reponse.SimpleResponse
import scraper.service.model.Pipeline
import scraper.service.model.PipelineTask
import scraper.service.repository.PipelineRepository
import scraper.service.repository.PipelineTaskRepository

import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api/pipeline")
class PipelineController {

    private static Class DEFAULT_BROWSER = Phantom.class;

    @Autowired
    PipelineRepository pipelineRepository;

    @Autowired
    PipelineTaskRepository pipelineTaskRepository;

    @RequestMapping('/{id}')
    Pipeline get(@PathVariable String id) {
        return pipelineRepository.findOne(id);
    }

    /**
     * Runs pipeline process by pipeline id.
     * @param id
     * @throws UnknownHostException
     */
    @RequestMapping("/run/{id}")
    void run(@PathVariable String id) throws UnknownHostException {
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
     * Runs pipeline process by pipeline id.
     * @param id
     * @throws UnknownHostException
     */
    @RequestMapping("/run-child/{childId}/{parentTaskId}")
    void runChild(@PathVariable String childId, @PathVariable String parentTaskId) throws UnknownHostException {
        PipelineTask pipelineParentTask = pipelineTaskRepository.findOne(parentTaskId);
        Pipeline pipelineEntityChild = pipelineRepository.findOne(childId);
        Store parentStore = runPipeline(pipelineEntityChild, pipelineParentTask.data as List<HashMap<String, String>>);
        runPipeline(pipelineEntityChild, parentStore.getData());
    }

    /**
     * Runs pipeline process by pipeline id.
     * @param id
     * @throws UnknownHostException
     */
    @RequestMapping("/run-in-sequence/{childId}/{parentId}")
    void runInSequence(@PathVariable String childId, @PathVariable String parentId) throws UnknownHostException {
        Pipeline pipelineEntityParent = pipelineRepository.findOne(parentId);
        Pipeline pipelineEntityChild = pipelineRepository.findOne(childId);

        Store prentStore = runPipeline(pipelineEntityParent, null);
        runPipeline(pipelineEntityChild, prentStore.getData());
    }

    /**
     *
     * @param pipelineEntity
     * @param runtimeData
     * @return
     */
    protected Store runPipeline(Pipeline pipelineEntity, List<HashMap<String, String>> runtimeData) {
        PipelineTask pipelineTaskParent = new PipelineTask();
        pipelineTaskParent.startOn = new Date();
        Store store;
        try {
            PipelineProcess pipelineProcess = getPipelineProcess(pipelineEntity, runtimeData);
            pipelineProcess.run();
            store = pipelineProcess.getStore();
            pipelineTaskParent.data = store.getData();
            pipelineTaskParent.pipeline = pipelineEntity;
        } catch (all) {
            println(all.printStackTrace());
            pipelineTaskParent.error = all.printStackTrace();
        }

        pipelineTaskParent.endOn = new Date();
        pipelineTaskRepository.save(pipelineTaskParent);
        return store;
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
