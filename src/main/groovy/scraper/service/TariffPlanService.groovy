package scraper.service

import groovy.json.JsonBuilder
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import scraper.dto.model.UpdateDeploymentDto
import scraper.dto.model.UserTariffPlanSubscriptionDto
import scraper.model.TariffPlan
import scraper.model.TariffPlanSubscription
import scraper.repository.TariffPlanRepository
import scraper.repository.TariffPlanSubscriptionRepository

@Service
class TariffPlanService {
    @Value('${app.version}')
    String appVersion

    @Value('${workermanager.url}')
    String workerManagerUrl

    @Autowired
    TariffPlanRepository tariffPlanRepository

    @Autowired
    TariffPlanSubscriptionRepository tariffPlanSubscriptionRepository

    void subscribe(UserTariffPlanSubscriptionDto dto) {
        def tariffPlan = tariffPlanRepository.findByKey(dto.tariffPlanKey)
        def tariffPlanSubscription = tariffPlanSubscriptionRepository.findByUserId(dto.userId)
        if (!tariffPlanSubscription) {
            tariffPlanSubscription = new TariffPlanSubscription(
                userId: dto.userId,
                tariffPlanId: tariffPlan.id,
                startedOnUtc: new Date(),
                expiredOnUtc: new Date() + 30
            )
        }
        tariffPlanSubscription.tariffPlanId = tariffPlan.id
        tariffPlanSubscriptionRepository.save(tariffPlanSubscription)
        updateDeployment(dto.userId, tariffPlan.configuration.privateWorkers)
    }

    List<TariffPlan> getAll() {
        return tariffPlanRepository.findAll()
    }

    TariffPlan getTariffPlanByUserId(String userId) {
        def tariffPlanSubscription = tariffPlanSubscriptionRepository.findByUserId(userId)
        if (!tariffPlanSubscription) {
            return tariffPlanRepository.findByKey('free')
        }
        def tariffPlan = tariffPlanRepository.findById(tariffPlanSubscription.tariffPlanId)
        return tariffPlan.present ? tariffPlan.get() : null
    }

    private updateDeployment(String userId, Number replicas) {
        CloseableHttpClient httpClient = HttpClients.createDefault()
        try {
            UpdateDeploymentDto dto = new UpdateDeploymentDto(
                userId: userId,
                mode: appVersion == 'development' ? 'dev': 'prod',
                replicas: replicas
            )
            String json = new JsonBuilder(dto).toPrettyString()
            StringEntity requestEntity = new StringEntity(
                    json,
                    ContentType.APPLICATION_JSON
            )
            String url = workerManagerUrl + '/deployments/template'
            HttpPost postMethod = new HttpPost(url)
            postMethod.setEntity(requestEntity)
            httpClient.execute(postMethod)
            httpClient.close()
        } catch (e) {
            httpClient.close()
            println e
        }
    }
}
