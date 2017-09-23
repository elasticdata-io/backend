package scraper.service.controller.listener

import org.springframework.messaging.simp.SimpMessagingTemplate
import scraper.core.browser.BrowserProvider
import scraper.core.command.AbstractCommand
import scraper.core.pipeline.data.AbstractStore
import scraper.service.model.PipelineTask

class PipelineBrowserProviderObserver implements Observer {

    BrowserProvider browserProvider
    SimpMessagingTemplate messagingTemplate
    PipelineTask pipelineTask

    PipelineBrowserProviderObserver(BrowserProvider browserProvider, SimpMessagingTemplate messagingTemplate,
                                    PipelineTask pipelineTask) {
        this.browserProvider = browserProvider
        this.messagingTemplate = messagingTemplate
        this.pipelineTask = pipelineTask
    }

    @Override
    void update(Observable o, Object arg) {
        List<AbstractCommand> commands = browserProvider.states
        AbstractCommand command = commands.last()
        def data = [
                pipelineId: pipelineTask.pipeline.id,
                command: command.getClass().getSimpleName(),
                params: command.toString()
        ]
        messagingTemplate.convertAndSend("/pipeline/command/execute", data)
    }
}
