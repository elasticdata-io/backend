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
import scraper.service.model.PipelineTask
import scraper.service.repository.PipelineHookRepository
import scraper.service.service.PipelineTaskService

@Component
class FinishPipelineTaskConsumer {

    final String DATA_MARKER = '#DATA#'
    final String PIPELINE_KEY = '#PIPELINE_KEY#'
    final String PIPELINE_TASK_ID = '#TASK_ID#'
    final String DOWNLOAD_LINK = '#DOWNLOAD_LINK#'
    final String TOTAL_RECORDS = '#TOTAL_RECORDS#'

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    PipelineTaskService pipelineTaskService

    @Autowired
    PipelineHookRepository pipelineHookRepository

    /**
     * Listener for run pipelineTask.
     * @param pipelineTaskId Id of the finished task pipelineId.
     */
    @RabbitListener(queues = QueueConstants.PIPELINE_TASK_FINISH, containerFactory="defaultConnectionFactory")
    void worker(String pipelineTaskId) {
        PipelineTask pipelineTask = pipelineTaskService.findById(pipelineTaskId)
        Pipeline pipeline = pipelineTask.pipeline
        // todo: web hooks possible not one!
        PipelineHook pipelineHook = pipelineHookRepository.findOneByPipeline(pipeline)
        if (!pipelineHook) {
            logger.info("HOOKS not fond. pipeline: ${pipeline.id}")
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
