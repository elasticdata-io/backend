package scraper.service.dto.mapper

import org.springframework.stereotype.Component
import scraper.service.dto.model.pipeline.PipelineStatusDto
import scraper.service.model.PipelineStatus

@Component
class PipelineStatusMapper {

    static PipelineStatusDto toPipelineStatusDto(PipelineStatus pipelineStatus) {
        if (!pipelineStatus) {
            return null
        }
        return new PipelineStatusDto(
            id: pipelineStatus.id,
            title: pipelineStatus.title,
        )
    }

    static PipelineStatus toPipelineStatus(PipelineStatusDto pipelineStatusDto) {
        if (!pipelineStatusDto) {
            return null
        }
        return new PipelineStatus(
            id: pipelineStatusDto.id,
            title: pipelineStatusDto.title,
        )
    }
}
