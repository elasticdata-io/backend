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
class PipelineFinishedConsumer {

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    QueueConstants queueConstants

    /**
     * Listener for run pipelineTask.
     * @param pipelineTaskId Id of the finished task pipelineId.
     */
    @RabbitListener(queues = '#{queueConstants.PIPELINE_FINISHED}', containerFactory="defaultConnectionFactory")
    void worker(String pipelineTaskId) {
        println pipelineTaskId
    }
}
