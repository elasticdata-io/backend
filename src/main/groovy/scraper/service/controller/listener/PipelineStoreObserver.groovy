package scraper.service.controller.listener

import org.springframework.messaging.simp.SimpMessagingTemplate
import scraper.core.pipeline.data.AbstractStore
import scraper.service.model.PipelineTask
import scraper.service.model.User

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
                newParseRowsCount: store.getLinesCount(),
                pipelineTaskId: pipelineTask.id,
                pipelineId: pipelineTask.pipeline.id,
                line: dataParsed.last()
        ]
        User user = pipelineTask.pipeline.user
        String channel = '/pipeline/parsed-lines/' + user.id
        messagingTemplate.convertAndSend(channel, data)
    }
}
