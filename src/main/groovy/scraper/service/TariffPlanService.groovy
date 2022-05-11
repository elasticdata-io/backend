package scraper.service

import groovy.time.TimeCategory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import scraper.dto.model.UserTariffPlanSubscriptionDto
import scraper.model.TariffPlan
import scraper.model.TariffPlanSubscription
import scraper.repository.TariffPlanRepository
import scraper.repository.TariffPlanSubscriptionRepository
import scraper.service.workermanager.WorkerManagerClient

@Service
class TariffPlanService {
    @Value('${app.version}')
    String appVersion

    @Autowired
    TariffPlanRepository tariffPlanRepository

    @Autowired
    TariffPlanSubscriptionRepository tariffPlanSubscriptionRepository

    @Autowired
    WorkerManagerClient workerManagerClient

    void autoSubscribe(String userId) {
        def tariffPlan = tariffPlanRepository.findByKey('free')
        def tariffPlanSubscription = tariffPlanSubscriptionRepository.findByUserId(userId)
        if (!tariffPlanSubscription) {
            use( TimeCategory ) {
                tariffPlanSubscription = new TariffPlanSubscription(
                        userId: userId,
                        tariffPlanId: tariffPlan.id,
                        startedOnUtc: new Date(),
                        expiredOnUtc: new Date() + 30.days
                )
                tariffPlanSubscriptionRepository.save(tariffPlanSubscription)
            }
        }
    }

    void subscribe(UserTariffPlanSubscriptionDto dto) {
        def tariffPlan = tariffPlanRepository.findByKey(dto.tariffPlanKey)
        def tariffPlanSubscription = tariffPlanSubscriptionRepository.findByUserId(dto.userId)
        if (!tariffPlanSubscription) {
            use( TimeCategory ) {
                tariffPlanSubscription = new TariffPlanSubscription(
                    userId: dto.userId,
                    tariffPlanId: tariffPlan.id,
                    startedOnUtc: new Date(),
                    expiredOnUtc: new Date() + 30.days
                )
            }
        }
        tariffPlanSubscription.tariffPlanId = tariffPlan.id
        tariffPlanSubscriptionRepository.save(tariffPlanSubscription)
        // def mode = appVersion == 'development' ? 'dev': 'prod'
        // workerManagerClient.updateFromTemplate(dto.userId, tariffPlan.configuration.privateWorkers, mode)
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

    Boolean notFreeSubscription(String userId) {
        def tariffPlanSubscription = tariffPlanSubscriptionRepository.findByUserId(userId)
        if (!tariffPlanSubscription) {
            return false
        }
        def tariffPlanGetter = tariffPlanRepository.findById(tariffPlanSubscription.tariffPlanId)
        def tariffPlan = tariffPlanGetter.present ? tariffPlanGetter.get() : null
        return tariffPlan.key != 'free'
    }
}
