package scraper.service.dto.mapper

import org.springframework.stereotype.Component
import scraper.service.dto.model.user.PipelineStatusDto
import scraper.service.dto.model.user.UserDto
import scraper.service.model.PipelineStatus
import scraper.service.model.User

@Component
class PipelineStatusMapper {

    static PipelineStatusDto toPipelineStatusDto(PipelineStatus pipelineStatus) {
        return new PipelineStatusDto(
                id: pipelineStatus.id,
                title: pipelineStatus.title,
        )
    }

}
