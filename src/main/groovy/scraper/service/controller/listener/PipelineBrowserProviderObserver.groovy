package scraper.service.controller.listener

import scraper.core.browser.BrowserProvider
import scraper.core.command.AbstractCommand
import scraper.service.dto.model.pipeline.PipelineCommandExecuteDto
import scraper.service.model.PipelineTask
import scraper.service.model.User
import scraper.service.ws.PipelineWebsockerProducer

class PipelineBrowserProviderObserver implements Observer {

    BrowserProvider browserProvider
    PipelineWebsockerProducer pipelineWebsockerProducer
    PipelineTask pipelineTask

    PipelineBrowserProviderObserver(BrowserProvider browserProvider,
                                    PipelineWebsockerProducer pipelineWebsockerProducer, PipelineTask pipelineTask) {
        this.browserProvider = browserProvider
        this.pipelineWebsockerProducer = pipelineWebsockerProducer
        this.pipelineTask = pipelineTask
    }

    @Override
    void update(Observable o, Object arg) {
        List<AbstractCommand> commands = browserProvider.states
        AbstractCommand command = commands.last()
        User user = pipelineTask.pipeline.user
        def data = new PipelineCommandExecuteDto(
                pipelineId: pipelineTask.pipeline.id,
                commandExecutingName: command.getClass().getSimpleName(),
                userId: user.id
        )
        pipelineWebsockerProducer.commandExecute(data)
    }
}
