package scraper.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import scraper.model.TariffPlan;

public interface TariffPlanRepository extends MongoRepository<TariffPlan, String> {
    TariffPlan findByKey(String key);
}
