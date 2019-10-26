package scraper.service.controller.listener

import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import scraper.core.command.AbstractCommand
import scraper.service.dto.model.pipeline.PipelineCommandExecuteDto
import scraper.service.model.Task
import scraper.service.ws.PipelineWebsockerProducer
import io.reactivex.Observer

class PipelineStateCommandsObserver implements Observer {

    PipelineWebsockerProducer pipelineWebsockerProducer
    Task task

    PipelineStateCommandsObserver(PipelineWebsockerProducer pipelineWebsockerProducer, Task task) {
        this.pipelineWebsockerProducer = pipelineWebsockerProducer
        this.task = task
    }

    @Override
    void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    void onNext(@NonNull Object o) {
        AbstractCommand command = (AbstractCommand) o
        def data = new PipelineCommandExecuteDto(
                pipelineId: task.pipelineId,
                commandExecutingName: "${command.getClass().getSimpleName().toLowerCase()}",
                commandExecutingProperties: "${command.getHumanProperties()}",
                userId: task.userId
        )
        pipelineWebsockerProducer.commandExecute(data)
    }

    @Override
    void onError(@NonNull Throwable e) {

    }

    @Override
    void onComplete() {

    }
}
