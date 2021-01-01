package scraper.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import scraper.model.DynamicLink;

public interface DynamicLinkRepository extends MongoRepository<DynamicLink, String> {
    DynamicLink findByAlias(String alias);
    DynamicLink findByAbsoluteUrl(String absoluteUrl);
}
