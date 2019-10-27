package scraper.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import scraper.service.model.TaskQueue;

import java.util.List;
import java.util.Optional;

public interface TaskQueueRepository extends MongoRepository<TaskQueue, String> {
	Optional<TaskQueue> findById(String id);
	TaskQueue findOneByTaskId(String taskId);
	List<TaskQueue> findByStatusAndUserIdOrderByCreatedOnUtc(String status, String userId);
	List<TaskQueue> findByStatus(String status);
}
