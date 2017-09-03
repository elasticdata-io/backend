package scraper.service.controller.listener

import org.springframework.messaging.simp.SimpMessagingTemplate
import scraper.core.pipeline.data.AbstractStore
import scraper.service.model.PipelineTask

class PipelineStoreObserver implements Observer {

    AbstractStore store
    SimpMessagingTemplate messagingTemplate
    PipelineTask pipelineTask

    PipelineStoreObserver (AbstractStore store, SimpMessagingTemplate messagingTemplate, PipelineTask pipelineTask) {
        this.store = store
        this.messagingTemplate = messagingTemplate
        this.pipelineTask = pipelineTask
    }

    @Override
    void update(Observable o, Object arg) {
        def dataParsed = store.getData()
        def data = [
                lastParsedLinesCount: dataParsed.size(),
                pipelineId: pipelineTask.pipeline.id,
                line: dataParsed.last()
        ]
        messagingTemplate.convertAndSend("/pipeline/parsed-lines", data)
    }
}
