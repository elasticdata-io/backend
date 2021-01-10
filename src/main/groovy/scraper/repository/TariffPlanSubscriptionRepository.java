package scraper.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import scraper.model.TariffPlanSubscription;

public interface TariffPlanSubscriptionRepository extends MongoRepository<TariffPlanSubscription, String> {
    TariffPlanSubscription findByUserId(String userId);
}
