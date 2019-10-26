package scraper.service.service

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.service.dto.mapper.PipelineMapper
import scraper.service.model.Pipeline
import scraper.service.repository.PipelineRepository

@Service
class PipelineService {

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    private PipelineRepository pipelineRepository

    Pipeline findById(String id) {
        Optional<Pipeline> pipeline = pipelineRepository.findById(id)
        return pipeline.present ? pipeline.get() : null
    }

    void save(Pipeline pipeline) {
        pipelineRepository.save(pipeline)
    }
}
