package scraper.service.dto.mapper

import org.springframework.stereotype.Component
import scraper.service.dto.model.task.PendingApiTaskDto
import scraper.service.dto.model.task.PendingTaskDto
import scraper.service.dto.model.task.TaskDto
import scraper.service.dto.model.user.UserDto
import scraper.service.model.Task
import scraper.service.model.User

@Component
class TaskMapper {

    static PendingApiTaskDto toPendingApiTaskDto(Task task) {
        return new PendingApiTaskDto(
                id: task.id,
                pipelineId: task.pipelineId,
                userId: task.userId,
                startOnUtc: task.startOnUtc,
                endOnUtc: task.endOnUtc,
                commands: task.commands,
                status: task.status,
                failureReason: task.failureReason,
                hookUrl: task.hookUrl,
                docsUrl: task.docsUrl
        )
    }

    static PendingTaskDto toPendingTaskDto(Task task) {
        return new PendingTaskDto(
                id: task.id,
                pipelineId: task.pipelineId,
                userId: task.userId,
                startOnUtc: task.startOnUtc,
                endOnUtc: task.endOnUtc,
                commands: task.commands,
                status: task.status,
                failureReason: task.failureReason,
                hookUrl: task.hookUrl,
                docsUrl: task.docsUrl
        )
    }

    static TaskDto toTaskDto(Task task) {
        return new TaskDto(
                id: task.id,
                pipelineId: task.pipelineId,
                userId: task.userId,
                startOnUtc: task.startOnUtc,
                endOnUtc: task.endOnUtc,
                status: task.status,
                failureReason: task.failureReason,
                hookUrl: task.hookUrl,
        )
    }

}
