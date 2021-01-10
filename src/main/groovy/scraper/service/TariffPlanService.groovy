package scraper.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.model.TariffPlan
import scraper.repository.TariffPlanRepository
import scraper.repository.TariffPlanSubscriptionRepository

@Service
class TariffPlanService {

    @Autowired
    TariffPlanRepository tariffPlanRepository

    @Autowired
    TariffPlanSubscriptionRepository tariffPlanSubscriptionRepository

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
}
