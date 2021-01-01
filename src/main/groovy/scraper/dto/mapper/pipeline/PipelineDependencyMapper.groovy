package scraper.dto.mapper.pipeline

import org.springframework.stereotype.Component
import scraper.dto.model.pipeline.PipelineDependencyDto

@Component
class PipelineDependencyMapper {

    static List<scraper.model.PipelineDependency> toPipelineDependencies(List<PipelineDependencyDto> pipelineDependenciesDto) {
        def list = new ArrayList<scraper.model.PipelineDependency>()
        pipelineDependenciesDto.each {x ->
            list.add(toPipelineDependency(x))
        }
        return list
    }

    static scraper.model.PipelineDependency toPipelineDependency(PipelineDependencyDto pipelineDependencyDto) {
        return new scraper.model.PipelineDependency(
                pipelineId: pipelineDependencyDto.pipelineId,
                dataFreshnessInterval: pipelineDependencyDto.dataFreshnessInterval,
        )
    }

    static PipelineDependencyDto toPipelineDependencyDto(scraper.model.PipelineDependency pipelineDependency) {
        return new PipelineDependencyDto(
                pipelineId: pipelineDependency.pipelineId,
                dataFreshnessInterval: pipelineDependency.dataFreshnessInterval,
        )
    }

    static List<PipelineDependencyDto> toPipelineDependenciesDto(List<scraper.model.PipelineDependency> pipelineDependencies) {
        def list = new ArrayList<PipelineDependencyDto>()
        pipelineDependencies.each {x ->
            list.add(toPipelineDependencyDto(x))
        }
        return list
    }

}
