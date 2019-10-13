package scraper.service.controller.listener

import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import scraper.core.command.AbstractCommand
import scraper.service.dto.model.pipeline.PipelineCommandExecuteDto
import scraper.service.model.PipelineTask
import scraper.service.model.User
import scraper.service.ws.PipelineWebsockerProducer
import io.reactivex.Observer

class PipelineStateCommandsObserver implements Observer {

    PipelineWebsockerProducer pipelineWebsockerProducer
    PipelineTask pipelineTask

    PipelineStateCommandsObserver(PipelineWebsockerProducer pipelineWebsockerProducer, PipelineTask pipelineTask) {
        this.pipelineWebsockerProducer = pipelineWebsockerProducer
        this.pipelineTask = pipelineTask
    }

    @Override
    void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    void onNext(@NonNull Object o) {
        AbstractCommand command = (AbstractCommand) o
        User user = pipelineTask.pipeline.user
        def data = new PipelineCommandExecuteDto(
                pipelineId: pipelineTask.pipeline.id,
                commandExecutingName: "${command.getClass().getSimpleName().toLowerCase()}",
                commandExecuting: "${command.toString()}",
                userId: user.id
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
