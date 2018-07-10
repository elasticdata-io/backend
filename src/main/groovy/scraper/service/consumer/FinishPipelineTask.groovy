package scraper.service.consumer

import groovy.json.JsonBuilder
import groovyx.net.http.HTTPBuilder
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.service.model.Pipeline
import scraper.service.model.PipelineHook
import scraper.service.model.PipelineTask
import scraper.service.repository.PipelineHookRepository
import scraper.service.repository.PipelineTaskRepository

@Component
class FinishPipelineTask {

    final String DATA_MARKER = '#DATA#'
    final String PIPELINE_KEY = '#PIPELINE_KEY#'
    final String PIPELINE_TASK_ID = '#TASK_ID#'
    final String DOWNLOAD_LINK = '#DOWNLOAD_LINK#'
    final String TOTAL_RECORDS = '#TOTAL_RECORDS#'

    @Autowired
    PipelineTaskRepository pipelineTaskRepository

    @Autowired
    PipelineHookRepository pipelineHookRepository

    /**
     * Listener for run pipelineTask.
     * @param pipelineTaskId Id of the finished task id.
     */
    @RabbitListener(queues = "finish-pipeline-task", containerFactory="multipleListenerContainerFactory")
    void worker(String pipelineTaskId) {
        PipelineTask pipelineTask = pipelineTaskRepository.findOne(pipelineTaskId)
        Pipeline pipeline = pipelineTask.pipeline
        PipelineHook pipelineHook = pipelineHookRepository.findOneByPipeline(pipeline)
        if (!pipelineHook) {
            return
        }
        String url = pipelineHook.hookUrl
        List list = pipelineTask.data as ArrayList
        String json = new JsonBuilder(pipelineTask.data).toPrettyString()
        String data = pipelineHook.jsonConfig.replaceAll(DATA_MARKER, json)
        data = data.replaceAll(PIPELINE_KEY, pipeline.key)
        data = data.replaceAll(PIPELINE_TASK_ID, pipelineTask.id)
        data = data.replaceAll(DOWNLOAD_LINK, "/api/pipeline-task/data/${pipelineTask.id}")
        data = data.replaceAll(TOTAL_RECORDS, list.size() as String)
        def http = new HTTPBuilder(url)
        try {
            http.post(body: data)
        } catch (ignored) {}
    }
}
