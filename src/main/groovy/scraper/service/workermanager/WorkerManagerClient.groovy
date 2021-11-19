package scraper.service.workermanager

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.apache.http.HttpEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import scraper.dto.model.ScaleDeploymentDto
import scraper.dto.model.UpdateDeploymentDto

@Service
class WorkerManagerClient {

    @Value('${workermanager.url}')
    String workerManagerUrl

    void updateFromTemplate(String userId, Number replicas, String mode) {
        UpdateDeploymentDto dto = new UpdateDeploymentDto(
            userId: userId,
            mode: mode,
            replicas: replicas
        )
        post('/deployments/template', dto)
    }

    void scale(String userId, Number replicas) {
        ScaleDeploymentDto dto = new ScaleDeploymentDto(
            userId: userId,
            replicas: replicas
        )
        post('/deployments/scale', dto)
    }

    Number fetchReplicas(String userId) {
        List result = get("/deployments/findByUser/${userId}") as List<HashMap>
        if (!result || result.isEmpty()) {
            return 0
        }
        def deployment = result.get(0)
        def spec = deployment?.get('spec') as HashMap
        return spec?.get('replicas') as Number
    }

    private Object post(String path, Object dto) {
        CloseableHttpClient httpClient = HttpClients.createDefault()
        try {
            String json = new JsonBuilder(dto).toPrettyString()
            StringEntity requestEntity = new StringEntity(
                json,
                ContentType.APPLICATION_JSON
            )
            String url = workerManagerUrl + path
            HttpPost postMethod = new HttpPost(url)
            postMethod.setEntity(requestEntity)
            CloseableHttpResponse response = httpClient.execute(postMethod)
            try {
                HttpEntity entity = response.getEntity()
                if (entity != null) {
                    String jsonResponse = EntityUtils.toString(entity)
                    if (jsonResponse) {
                        def jsonSlurper = new JsonSlurper()
                        return jsonSlurper.parseText(jsonResponse)
                    }
                }
            } catch (e) {
                println e
                response.close()
            }
        } catch (e) {
            println e
        } finally {
            httpClient.close()
        }
    }

    private Object get(String path) {
        CloseableHttpClient httpClient = HttpClients.createDefault()
        try {
            String url = workerManagerUrl + path
            HttpGet getMethod = new HttpGet(url)
            CloseableHttpResponse response = httpClient.execute(getMethod)
            try {
                HttpEntity entity = response.getEntity()
                if (entity != null) {
                    String json = EntityUtils.toString(entity)
                    def jsonSlurper = new JsonSlurper()
                    return jsonSlurper.parseText(json)
                }
                response.close()
                return entity
            } catch (e) {
                println e
                response.close()
            }
        } catch (e) {
            println e
        } finally {
            httpClient.close()
        }
    }
}
