package scraper.service.amqp.consumer

import groovy.json.JsonBuilder
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.service.amqp.QueueConstants
import scraper.service.dto.mapper.TaskMapper
import scraper.service.dto.model.task.PendingApiTaskDto
import scraper.service.model.Pipeline
import scraper.service.model.Task
import scraper.service.service.PipelineService
import scraper.service.service.TaskService

@Component
class RunHooksConsumer {

    final String DATA_MARKER = '#DATA#'
    final String PIPELINE_KEY = '#PIPELINE_KEY#'
    final String PIPELINE_TASK_ID = '#TASK_ID#'
    final String DOWNLOAD_LINK = '#DOWNLOAD_LINK#'
    final String TOTAL_RECORDS = '#TOTAL_RECORDS#'

    @Autowired
    QueueConstants queueConstants

    @Autowired
    TaskService taskService

    @Autowired
    PipelineService pipelineService

    // @RabbitListener(queues = '#{queueConstants.RUN_HOOKS}', containerFactory="defaultConnectionFactory")
    void worker(String taskId) {
        Task task = taskService.findById(taskId)
        Pipeline pipeline = pipelineService.findById(task.pipelineId)
        String url = task.hookUrl
        if (!url) {
            // logger.info("hook url not found, pipeline: ${pipeline.id}")
            return
        }
        //List list = task.docs as ArrayList
        //String json = new JsonBuilder(task.docs).toPrettyString()
        //String data = pipelineHook.jsonConfig.replaceAll(DATA_MARKER, json)
        //data = data.replaceAll(PIPELINE_KEY, pipeline.key)
        //data = data.replaceAll(PIPELINE_TASK_ID, task.id)
        //data = data.replaceAll(DOWNLOAD_LINK, "/api/pipeline-task/data/${task.id}")
        //data = data.replaceAll(TOTAL_RECORDS, list.size() as String)
        //def jsonSlurper = new JsonSlurper()
        //def dataList = jsonSlurper.parseText(data)
        // logger.info("start hooks ${url}, pipeline: ${pipeline.id}")
        CloseableHttpClient httpClient = HttpClients.createDefault()
        try {
            PendingApiTaskDto pendingApiTaskDto = TaskMapper.toPendingApiTaskDto(task)
            String json = new JsonBuilder(pendingApiTaskDto).toPrettyString()
//            String json = fileDataRepository.getDataFileToString(task)
            StringEntity requestEntity = new StringEntity(
                    json,
                    ContentType.APPLICATION_JSON
            )
            HttpPost postMethod = new HttpPost(url)
            postMethod.setEntity(requestEntity)
            httpClient.execute(postMethod)
            httpClient.close()
            // logger.info("hook for url is ${url} successful")
        } catch (e) {
            // logger.error(e)
            httpClient.close()
        }
    }
}
