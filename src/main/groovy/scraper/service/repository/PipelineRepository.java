package scraper.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import scraper.service.model.Pipeline;
import scraper.service.model.User;

import java.util.Collection;
import java.util.List;

public interface PipelineRepository extends MongoRepository<Pipeline, String> {

	List<Pipeline> findByUser(String userId);
	Pipeline findByIdAndUser(String id, String userId);
	List<Pipeline> findByUser(User user);
	List<Pipeline> findByUserAndIdNotIn(String userId, List<String> dependsOn);
	List<Pipeline> findByUserAndIdNotIn(User user, List<String> dependsOn);
	List<Pipeline> findByDependOn(String dependOn);
}
