package scraper.service.amqp.consumer

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.apache.http.HttpEntity
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.service.amqp.QueueConstants
import scraper.service.model.Pipeline
import scraper.service.model.PipelineHook
import scraper.service.model.Task
import scraper.service.repository.PipelineHookRepository
import scraper.service.service.PipelineService
import scraper.service.service.TaskService

@Component
class RunHooksConsumer {

    final String DATA_MARKER = '#DATA#'
    final String PIPELINE_KEY = '#PIPELINE_KEY#'
    final String PIPELINE_TASK_ID = '#TASK_ID#'
    final String DOWNLOAD_LINK = '#DOWNLOAD_LINK#'
    final String TOTAL_RECORDS = '#TOTAL_RECORDS#'

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    QueueConstants queueConstants

    @Autowired
    TaskService taskService

    @Autowired
    PipelineService pipelineService

    @Autowired
    PipelineHookRepository pipelineHookRepository

    @RabbitListener(queues = '#{queueConstants.PIPELINE_RUN_HOOKS}', containerFactory="defaultConnectionFactory")
    void worker(String pipelineTaskId) {
        Task task = taskService.findById(pipelineTaskId)
        Pipeline pipeline = pipelineService.findById(task.pipelineId)
        // todo: web hooks possible not one!
        PipelineHook pipelineHook = pipelineHookRepository.findOneByPipeline(pipeline)
        if (!pipelineHook) {
            logger.info("HOOKS not fond. task.id: ${task.id}")
            return
        }
        String url = pipelineHook.hookUrl
        List list = task.docs as ArrayList
        String json = new JsonBuilder(task.docs).toPrettyString()
        String data = pipelineHook.jsonConfig.replaceAll(DATA_MARKER, json)
        data = data.replaceAll(PIPELINE_KEY, pipeline.key)
        data = data.replaceAll(PIPELINE_TASK_ID, task.id)
        data = data.replaceAll(DOWNLOAD_LINK, "/api/pipeline-task/data/${task.id}")
        data = data.replaceAll(TOTAL_RECORDS, list.size() as String)
        def jsonSlurper = new JsonSlurper()
        def dataList = jsonSlurper.parseText(data)
        CloseableHttpClient httpClient = HttpClients.createDefault()
        HttpPost httpPost = new HttpPost(url)
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>()
        dataList.each { key, value ->
            urlParameters.add(new BasicNameValuePair(key, value))
        }
        HttpEntity postParams = new UrlEncodedFormEntity(urlParameters)
        httpPost.setEntity(postParams)
        logger.info("start hooks ${url}, pipeline: ${pipeline.id}")
        try {
            httpClient.execute(httpPost)
            httpClient.close()
        } catch (e) {
            logger.error(e)
            httpClient.close()
        }
    }
}
