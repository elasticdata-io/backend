package scraper.service.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import scraper.service.model.Pipeline;
import scraper.service.model.PipelineTask;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PipelineTaskRepository extends MongoRepository<PipelineTask, String> {
	Optional<PipelineTask> findById(String id);
	PipelineTask findByEndOnBetween(Date from, Date to);
	List<PipelineTask> findByPipelineOrderByEndOnDesc(String pipeline);
	List<PipelineTask> findByPipelineOrderByEndOnDesc(String pipeline, Pageable top);
	PipelineTask findOneByPipelineOrderByEndOnDesc(String pipeline);
	PipelineTask findOneByPipelineAndErrorOrderByEndOnDesc(String pipeline, String error);
}
