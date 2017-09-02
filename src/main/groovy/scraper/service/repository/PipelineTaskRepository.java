package scraper.service.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import scraper.service.model.PipelineTask;

import java.util.Date;
import java.util.List;

public interface PipelineTaskRepository extends MongoRepository<PipelineTask, String> {
	PipelineTask findByEndOnBetween(Date from, Date to);
	List<PipelineTask> findByPipelineOrderByEndOnDesc(String pipeline);
	List<PipelineTask> findByPipelineOrderByEndOnDesc(String pipeline, Pageable top);
	PipelineTask findOneByPipelineOrderByEndOnDesc(String pipeline);
}
