package scraper.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import scraper.service.model.DynamicLink;

public interface DynamicLinkRepository extends MongoRepository<DynamicLink, String> {
    DynamicLink findByAlias(String alias);
}
