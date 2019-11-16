package scraper.service.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import scraper.service.model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends MongoRepository<Task, String> {
	Optional<Task> findById(String id);
	List<Task> findByPipelineIdAndUserIdOrderByStartOnUtcDesc(String pipelineId, String userId, Pageable top);
	Task findFirstByPipelineIdAndFailureReasonOrderByEndOnUtcDesc(String pipelineId, String failureReason);
	List<Task> findByPipelineIdAndFailureReasonOrderByStartOnUtcDesc(String pipelineId, String failureReason, Pageable pageable);
	List<Task> findByPipelineIdAndStatusInOrderByStartOnUtcDesc(String pipelineId, List<String> statuses, Pageable pageable);
	List<Task> findByStatusInAndUserId(List<String> statuses, String userId);
	List<Task> findByStatusInAndUserIdOrderByStartOnUtcDesc(List<String> statuses, String userId);
	List<Task> findByStatusInAndUserIdOrderByStartOnUtcAsc(List<String> statuses, String userId);
	List<Task> findByStatusInOrderByStartOnUtcAsc(List<String> statuses, Pageable page);
}
