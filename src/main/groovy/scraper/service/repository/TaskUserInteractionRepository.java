package scraper.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import scraper.service.model.TaskUserInteraction;

import java.util.List;
import java.util.Optional;

public interface TaskUserInteractionRepository extends MongoRepository<TaskUserInteraction, String> {
	Optional<TaskUserInteraction> findById(String id);
	List<TaskUserInteraction> findByTaskId(String id);
	Optional<TaskUserInteraction> findByTaskIdAndPageContext(String id, String pageContext);
}