package scraper.service.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import scraper.core.browser.Browser
import scraper.core.browser.BrowserFactory
import scraper.core.browser.BrowserProvider
import scraper.core.browser.provider.Chrome
import scraper.core.pipeline.PipelineBuilder
import scraper.core.pipeline.data.Store
import scraper.service.model.Pipeline
import scraper.service.model.PipelineTask
import scraper.service.repository.PipelineRepository
import scraper.service.repository.PipelineTaskRepository

@RestController
@RequestMapping("/api/pipeline")
class PipelineController {

    @Autowired
    PipelineRepository pipelineRepository;

    @Autowired
    PipelineTaskRepository pipelineTaskRepository;

    @RequestMapping("/run/{id}")
    public void run(@PathVariable String id) throws UnknownHostException {
        Pipeline pipelineEntity = pipelineRepository.findOne(id);

        PipelineBuilder pipelineBuilder = new PipelineBuilder();

        def factory = new BrowserFactory();
        Browser browser = factory.createFromClass(Chrome);
        BrowserProvider driverProvider = new BrowserProvider(webDriver: browser.create());

        scraper.core.pipeline.Pipeline pipeline = pipelineBuilder.setPipelineJson(pipelineEntity.jsonCommands)
                .setDriverProvider(driverProvider).build();
        pipeline.run();
        Store store = pipeline.getStore();

        PipelineTask pipelineTask = new PipelineTask();
        pipelineTask.json = store.getJsonData();
        pipelineTask.pipeline = pipelineEntity;

        pipelineTaskRepository.save(pipelineTask);
    }
}
