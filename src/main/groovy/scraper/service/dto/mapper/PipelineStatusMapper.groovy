package scraper.service.dto.mapper

import org.springframework.stereotype.Component
import scraper.service.dto.model.pipeline.PipelineStatusDto
import scraper.service.model.PipelineStatus

@Component
class PipelineStatusMapper {

    static PipelineStatusDto toPipelineStatusDto(PipelineStatus pipelineStatus) {
        return new PipelineStatusDto(
                id: pipelineStatus.id,
                title: pipelineStatus.title,
        )
    }

}
