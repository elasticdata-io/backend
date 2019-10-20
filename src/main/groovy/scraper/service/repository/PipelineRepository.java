package scraper.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import scraper.service.model.Pipeline;
import scraper.service.model.PipelineStatus;
import scraper.service.model.User;

import java.util.List;
import java.util.Optional;

public interface PipelineRepository extends MongoRepository<Pipeline, String> {

	Optional<Pipeline> findById(String id);
	List<Pipeline> findByUserOrderByCreatedOnDesc(String userId);
	Pipeline findByIdAndUser(String id, String userId);
	List<Pipeline> findByUser(User user);
	List<Pipeline> findByUserAndIdNotIn(String userId, List<String> dependsOn);
	List<Pipeline> findByUserAndIdNotIn(User user, List<String> dependsOn);
	List<Pipeline> findByDependOn(String dependOn);
	List<Pipeline> findByStatus(PipelineStatus status);
	List<Pipeline> findByStatusNot(PipelineStatus status);

	@Query("{ 'dependencies' : {$elemMatch: {'pipelineId': ?0}}, 'status.id': ?1 }")
	List<Pipeline> findByDependenciesAndStatus(String dependencyPipelineId, String statusId);
}
