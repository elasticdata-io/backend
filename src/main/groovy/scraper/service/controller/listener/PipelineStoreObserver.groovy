package scraper.service.controller.listener

import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import scraper.service.dto.model.pipeline.TaskParsedLinesDto
import scraper.service.model.Task
import io.reactivex.Observer
import scraper.service.ws.TaskWebsocketProducer

class PipelineStoreObserver implements Observer {

    TaskWebsocketProducer taskWebsocketProducer
    Task task

    PipelineStoreObserver (TaskWebsocketProducer taskWebsocketProducer, Task task) {
        this.taskWebsocketProducer = taskWebsocketProducer
        this.task = task
    }

    @Override
    void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    void onNext(@NonNull Object o) {
        def lines = (List<HashMap<String, String>>) o
        def data = new TaskParsedLinesDto(
                newParseRowsCount: lines.size(),
                pipelineTaskId: task.id,
                pipelineId: task.pipelineId,
                line: lines.last(),
                userId: task.userId
        )
        taskWebsocketProducer.parsedLines(data)
    }

    @Override
    void onError(@NonNull Throwable e) {

    }

    @Override
    void onComplete() {

    }
}
