package scraper.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import scraper.service.model.PipelineTask;

public interface PipelineTaskRepository extends MongoRepository<PipelineTask, String> {

}
