package scraper.dto.mapper

import org.springframework.stereotype.Component
import scraper.dto.model.task.UserInteractionDto
import scraper.model.TaskUserInteraction

@Component
class UserInteractionMapper {

    static UserInteractionDto toUserInteractionDto(TaskUserInteraction taskUserInteraction) {
        def state = taskUserInteraction.lastPageState as HashMap<String, Object>
        return new UserInteractionDto(
                id: taskUserInteraction.id,
                jpegScreenshotLink: state.get('jpegScreenshotLink') as String,
                pageElements: state.get('pageElements') as List<HashMap>,
                currentUrl: state.get('currentUrl') as String,
                pageContext: taskUserInteraction.pageContext,
                taskId: taskUserInteraction.taskId,
                userId: taskUserInteraction.userId,
                pipelineId: taskUserInteraction.pipelineId,
                status: taskUserInteraction.status,
                createdOnUtc: taskUserInteraction.createdOnUtc,
                modifiedOnUtc: taskUserInteraction.modifiedOnUtc,
                endOnUtc: taskUserInteraction.endOnUtc,
                expiredOnUtc: taskUserInteraction.expiredOnUtc,
                pipelineStatus: taskUserInteraction.pipelineStatus,
                pageHeightPx: state.get('pageHeightPx') as Number,
                pageWidthPx: state.get('pageWidthPx') as Number,
        )
    }
}
