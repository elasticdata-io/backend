package scraper.service.dto.mapper

import org.springframework.stereotype.Component
import scraper.service.dto.model.task.HookTaskDto
import scraper.service.dto.model.task.PendingApiTaskDto
import scraper.service.dto.model.task.PendingTaskDto
import scraper.service.dto.model.task.TaskDto
import scraper.service.dto.model.task.TaskEditDto
import scraper.service.model.Task

@Component
class TaskMapper {

    static HookTaskDto toHookTaskDto(Task task) {
        return new HookTaskDto(
                taskId: task.id,
                pipelineId: task.pipelineId,
                userId: task.userId,
                startOnUtc: task.startOnUtc,
                endOnUtc: task.endOnUtc,
                status: task.status,
                failureReason: task.failureReason,
                hookUrl: task.hookUrl,
                docsUrl: task.docsUrl,
                pipelineVersion: task.pipelineVersion,
                docsCount: task.docsCount,
                docsBytes: task.docsBytes,
        )
    }

    static PendingApiTaskDto toPendingApiTaskDto(Task task) {
        def userInteraction = task.pipelineConfiguration?.settings?.userInteraction
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
                docsUrl: task.docsUrl,
                hasUserInteraction: userInteraction != null,
        )
    }

    static PendingTaskDto toPendingTaskDto(Task task) {
        def userInteraction = task.pipelineConfiguration?.settings?.userInteraction
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
                docsCount: task.docsCount,
                docsBytes: task.docsBytes,
                docsUrl: task.docsUrl,
                hasUserInteraction: userInteraction != null,
        )
    }

    static TaskDto toTaskDto(Task task) {
        def userInteraction = task.pipelineConfiguration?.settings?.userInteraction
        return new TaskDto(
                id: task.id,
                pipelineId: task.pipelineId,
                userId: task.userId,
                startOnUtc: task.startOnUtc,
                endOnUtc: task.endOnUtc,
                status: task.status,
                failureReason: task.failureReason,
                hookUrl: task.hookUrl,
                docsUrl: task.docsUrl,
                docsBytes: task.docsBytes,
                docsCount: task.docsCount,
                commandsInformationLink: task.commandsInformationLink,
                pipelineVersion: task.pipelineVersion,
                hasUserInteraction: userInteraction != null,
        )
    }

    static TaskEditDto toTaskEditDto(Task task) {
        def userInteraction = task.pipelineConfiguration?.settings?.userInteraction
        return new TaskEditDto(
                id: task.id,
                pipelineId: task.pipelineId,
                userId: task.userId,
                startOnUtc: task.startOnUtc,
                endOnUtc: task.endOnUtc,
                status: task.status,
                failureReason: task.failureReason,
                hookUrl: task.hookUrl,
                docsUrl: task.docsUrl,
                docsBytes: task.docsBytes,
                docsCount: task.docsCount,
                commands: task.commands,
                commandsInformationLink: task.commandsInformationLink,
                hasUserInteraction: userInteraction != null,
        )
    }
}
