package scraper.service.controller.listener

import scraper.core.pipeline.data.AbstractStore
import scraper.service.dto.model.pipeline.PipelineParsedLinesDto
import scraper.service.model.PipelineTask
import scraper.service.model.User
import scraper.service.ws.PipelineWebsockerProducer

class PipelineStoreObserver implements Observer {

    AbstractStore store
    PipelineWebsockerProducer pipelineWebsockerProducer
    PipelineTask pipelineTask

    PipelineStoreObserver (AbstractStore store, PipelineWebsockerProducer pipelineWebsockerProducer,
                           PipelineTask pipelineTask) {
        this.store = store
        this.pipelineWebsockerProducer = pipelineWebsockerProducer
        this.pipelineTask = pipelineTask
    }

    @Override
    void update(Observable o, Object arg) {
        def dataParsed = store.getData()
        User user = pipelineTask.pipeline.user
        def data = new PipelineParsedLinesDto(
                newParseRowsCount: store.getLinesCount(),
                pipelineTaskId: pipelineTask.id,
                pipelineId: pipelineTask.pipeline.id,
                line: dataParsed.last(),
                userId: user.id
        )
        pipelineWebsockerProducer.parsedLines(data)
    }
}
