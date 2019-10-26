package scraper.service.service

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.core.browser.BrowserProvider
import scraper.core.pipeline.PipelineProcess
import scraper.core.pipeline.data.ObservableStore
import scraper.service.controller.listener.PipelineStateCommandsObserver
import scraper.service.controller.listener.PipelineStoreObserver
import scraper.service.dto.mapper.PipelineMapper
import scraper.service.model.Pipeline
import scraper.service.model.Task
import scraper.service.repository.PipelineRepository
import scraper.service.ws.PipelineWebsockerProducer

@Service
class PipelineService {

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    private PipelineRepository pipelineRepository

    @Autowired
    private PipelineWebsockerProducer pipelineWebsockerProducer

    void notifyChangePipeline(Pipeline pipeline) {
        def pipelineDto = PipelineMapper.toPipelineDto(pipeline)
        pipelineWebsockerProducer.change(pipelineDto)
    }

    private void bindStoreObserver(PipelineProcess pipelineProcess, Task task) {
        ObservableStore store = pipelineProcess.getStore()
        def observer = new PipelineStoreObserver(pipelineWebsockerProducer, task)
        store.subscribe(observer)
    }

    private void bindCommandObserver(PipelineProcess pipelineProcess, Task task) {
        BrowserProvider browserProvider = pipelineProcess.getBrowserProvider()
        def observer = new PipelineStateCommandsObserver(pipelineWebsockerProducer, task)
        browserProvider.subscribe(observer)
    }

    Pipeline findById(String id) {
        Optional<Pipeline> pipeline = pipelineRepository.findById(id)
        return pipeline.present ? pipeline.get() : null
    }

    void save(Pipeline pipeline) {
        pipelineRepository.save(pipeline)
    }
}
