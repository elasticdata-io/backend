package scraper.service.controller.listener

import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import scraper.service.dto.model.pipeline.PipelineParsedLinesDto
import scraper.service.model.PipelineTask
import scraper.service.model.User
import scraper.service.ws.PipelineWebsockerProducer
import io.reactivex.Observer

class PipelineStoreObserver implements Observer {

    PipelineWebsockerProducer pipelineWebsockerProducer
    PipelineTask pipelineTask

    PipelineStoreObserver (PipelineWebsockerProducer pipelineWebsockerProducer, PipelineTask pipelineTask) {
        this.pipelineWebsockerProducer = pipelineWebsockerProducer
        this.pipelineTask = pipelineTask
    }

    @Override
    void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    void onNext(@NonNull Object o) {
        def lines = (List<HashMap<String, String>>) o
        User user = pipelineTask.pipeline.user
        def data = new PipelineParsedLinesDto(
                newParseRowsCount: lines.size(),
                pipelineTaskId: pipelineTask.id,
                pipelineId: pipelineTask.pipeline.id,
                line: lines.last(),
                userId: user.id
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
