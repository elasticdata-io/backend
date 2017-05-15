package scraper.service.repository;

import org.springframework.data.repository.Repository;
import scraper.service.model.Pipeline;

import java.util.List;

public interface PipelineRepository extends Repository<Pipeline, Long> {

	List<Pipeline> findByUser(String user);
}
