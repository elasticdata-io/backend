package scraper.service.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.service.model.Pipeline
import scraper.service.repository.PipelineRepository

@Service
class PipelineStructure {

    private static final Logger logger = LoggerFactory.getLogger(PipelineStructure.class)

    @Autowired
    private PipelineRepository pipelineRepository

    List<String> getPipelineHierarchy(String pipelineId) {
        return recursiveAppendPipelineDependOn([pipelineId])
    }

    private List<String> recursiveAppendPipelineDependOn(List<String> pipelineIdList) {
        String lastPipelineId = pipelineIdList.last()
        Pipeline lastPipeline = pipelineRepository.findOne(lastPipelineId)
        Pipeline dependOn = lastPipeline.dependOn
        if (!dependOn) {
            return pipelineIdList
        }
        pipelineIdList.add(dependOn.id)
        return recursiveAppendPipelineDependOn(pipelineIdList)
    }
}