package scraper.service.controller.listener

import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import scraper.service.dto.model.pipeline.PipelineParsedLinesDto
import scraper.service.model.Task
import scraper.service.ws.PipelineWebsockerProducer
import io.reactivex.Observer

class PipelineStoreObserver implements Observer {

    PipelineWebsockerProducer pipelineWebsockerProducer
    Task task

    PipelineStoreObserver (PipelineWebsockerProducer pipelineWebsockerProducer, Task task) {
        this.pipelineWebsockerProducer = pipelineWebsockerProducer
        this.task = task
    }

    @Override
    void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    void onNext(@NonNull Object o) {
        def lines = (List<HashMap<String, String>>) o
        def data = new PipelineParsedLinesDto(
                newParseRowsCount: lines.size(),
                pipelineTaskId: task.id,
                pipelineId: task.pipelineId,
                line: lines.last(),
                userId: task.userId
        )
        pipelineWebsockerProducer.parsedLines(data)
    }

    @Override
    void onError(@NonNull Throwable e) {

    }

    @Override
    void onComplete() {

    }
}
