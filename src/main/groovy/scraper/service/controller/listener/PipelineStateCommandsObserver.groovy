package scraper.service.controller.listener

import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import scraper.core.command.AbstractCommand
import scraper.service.dto.model.task.command.TaskCommandExecuteDto
import scraper.service.model.Task
import io.reactivex.Observer
import scraper.service.ws.TaskWebsocketProducer

class PipelineStateCommandsObserver implements Observer {

    TaskWebsocketProducer taskWebsocketProducer
    Task task

    PipelineStateCommandsObserver(TaskWebsocketProducer taskWebsocketProducer, Task task) {
        this.taskWebsocketProducer = taskWebsocketProducer
        this.task = task
    }

    @Override
    void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    void onNext(@NonNull Object o) {
        AbstractCommand command = (AbstractCommand) o
        def data = new TaskCommandExecuteDto(
                pipelineId: task.pipelineId,
                taskId: task.id,
                cmd: "${command.getClass().getSimpleName().toLowerCase()}",
                runTimeProperties: "${command.getHumanProperties()}",
                userId: task.userId
        )
        taskWebsocketProducer.startCommandExecute(data)
    }

    @Override
    void onError(@NonNull Throwable e) {

    }

    @Override
    void onComplete() {

    }
}
