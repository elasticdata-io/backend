package scraper.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import scraper.service.model.PipelineStatus;

public interface PipelineStatusRepository extends MongoRepository<PipelineStatus, String> {
	PipelineStatus findByTitle(String title);
}
