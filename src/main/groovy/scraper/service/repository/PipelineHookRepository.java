package scraper.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import scraper.service.model.Pipeline;
import scraper.service.model.PipelineHook;

public interface PipelineHookRepository extends MongoRepository<PipelineHook, String> {
	PipelineHook findOneByPipeline(Pipeline pipeline);
}
