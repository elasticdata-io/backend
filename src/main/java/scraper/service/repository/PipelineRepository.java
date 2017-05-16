package scraper.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import scraper.service.model.Pipeline;

import java.util.List;

public interface PipelineRepository extends MongoRepository<Pipeline, String> {

	List<Pipeline> findByUser(@Param("user") String user);

}
