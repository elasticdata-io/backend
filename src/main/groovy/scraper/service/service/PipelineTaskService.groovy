package scraper.service.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.service.model.PipelineTask
import scraper.service.repository.PipelineTaskRepository

@Service
class PipelineTaskService {

    @Autowired
    PipelineTaskRepository pipelineTaskRepository

    PipelineTask findById(String id) {
        Optional<PipelineTask> pipeline = pipelineTaskRepository.findById(id)
        return pipeline.present ? pipeline : null
    }

}
