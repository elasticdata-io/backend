package scraper.service.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.service.model.PipelineTask

@Component
class PipelineTaskService {

    @Autowired
    PipelineTaskRepository

    PipelineTask findById(String id) {
        Optional<PipelineTask> pipeline = pipelineTaskRepository.findById(id)
        return pipeline.present ? pipeline : null
    }

}
