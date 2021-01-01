package scraper.dto.mapper.pipeline

import org.springframework.stereotype.Component
import scraper.dto.model.pipeline.PipelineDependencyDto
import scraper.model.PipelineDependency

@Component
class PipelineDependencyMapper {

    static List<PipelineDependency> toPipelineDependencies(List<PipelineDependencyDto> pipelineDependenciesDto) {
        def list = new ArrayList<PipelineDependency>()
        pipelineDependenciesDto.each {x ->
            list.add(toPipelineDependency(x))
        }
        return list
    }

    static PipelineDependency toPipelineDependency(PipelineDependencyDto pipelineDependencyDto) {
        return new PipelineDependency(
                pipelineId: pipelineDependencyDto.pipelineId,
                dataFreshnessInterval: pipelineDependencyDto.dataFreshnessInterval,
        )
    }

    static PipelineDependencyDto toPipelineDependencyDto(PipelineDependency pipelineDependency) {
        return new PipelineDependencyDto(
                pipelineId: pipelineDependency.pipelineId,
                dataFreshnessInterval: pipelineDependency.dataFreshnessInterval,
        )
    }

    static List<PipelineDependencyDto> toPipelineDependenciesDto(List<PipelineDependency> pipelineDependencies) {
        def list = new ArrayList<PipelineDependencyDto>()
        pipelineDependencies.each {x ->
            list.add(toPipelineDependencyDto(x))
        }
        return list
    }

}
