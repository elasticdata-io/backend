package scraper.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import scraper.model.Pipeline;

import java.util.List;
import java.util.Optional;

public interface PipelineRepository extends MongoRepository<Pipeline, String> {

	Optional<Pipeline> findById(String id);
	List<Pipeline> findByIdIn(List<String> ids);
	List<Pipeline> findByUserOrderByModifiedOnDesc(String userId);
	Pipeline findByIdAndUser(String id, String userId);
	List<Pipeline> findByUserAndIdNotIn(String userId, List<String> dependsOn);
	List<Pipeline> findByStatusNot(String status);
	List<Pipeline> findByRunIntervalMinIsNotNull();
}
