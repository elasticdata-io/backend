package scraper.service.amqp.consumer

import groovy.json.JsonBuilder
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.service.amqp.QueueConstants
import scraper.service.dto.mapper.TaskMapper
import scraper.service.dto.model.task.HookTaskDto
import scraper.service.model.Task
import scraper.service.service.TaskService

@Component
class RunHooksConsumer {
    private Logger logger = LogManager.getRootLogger()

    @Autowired
    QueueConstants queueConstants

    @Autowired
    TaskService taskService

    @RabbitListener(queues = '#{queueConstants.RUN_HOOK}', containerFactory="defaultConnectionFactory")
    void worker(String taskId) {
        Task task = taskService.findById(taskId)
        String url = task.hookUrl
        if (!url) {
            logger.info("hook url not found, taskId: ${taskId}")
            return
        }
        CloseableHttpClient httpClient = HttpClients.createDefault()
        try {
            HookTaskDto hookTaskDto = TaskMapper.toHookTaskDto(task)
            String json = new JsonBuilder(hookTaskDto).toPrettyString()
            StringEntity requestEntity = new StringEntity(
                json,
                ContentType.APPLICATION_JSON
            )
            HttpPost postMethod = new HttpPost(url)
            postMethod.setEntity(requestEntity)
            httpClient.execute(postMethod)
            httpClient.close()
        } catch (e) {
            httpClient.close()
            throw e
        }
    }
}
