package scraper.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import scraper.service.model.PipelineTask;

import java.util.Date;

public interface PipelineTaskRepository extends MongoRepository<PipelineTask, String> {
	PipelineTask findByEndOnBetween(Date from, Date to);
}
