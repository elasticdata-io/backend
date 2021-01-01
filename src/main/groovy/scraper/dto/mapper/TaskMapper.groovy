package scraper.dto.mapper

import groovy.json.JsonOutput
import org.springframework.stereotype.Component
import scraper.dto.model.task.PendingApiTaskDto
import scraper.dto.model.task.TaskEditDto

@Component
class TaskMapper {

    static scraper.dto.model.task.HookTaskDto toHookTaskDto(scraper.model.Task task) {
        return new scraper.dto.model.task.HookTaskDto(
                taskId: task.id,
                pipelineId: task.pipelineId,
                userId: task.userId,
                createdOnUtc: task.createdOnUtc,
                modifiedOnUtc: task.modifiedOnUtc,
                startOnUtc: task.startOnUtc,
                endOnUtc: task.endOnUtc,
                status: task.status,
                failureReason: task.failureReason,
                hookUrl: task.hookUrl,
                docsUrl: task.docsUrl,
                pipelineVersion: task.dsl?.version,
                docsCount: task.docsCount,
                docsBytes: task.docsBytes,
        )
    }

    static PendingApiTaskDto toPendingApiTaskDto(scraper.model.Task task) {
        def userInteraction = task.dsl?.settings?.userInteraction
        return new PendingApiTaskDto(
                id: task.id,
                pipelineId: task.pipelineId,
                userId: task.userId,
                createdOnUtc: task.createdOnUtc,
                modifiedOnUtc: task.modifiedOnUtc,
                startOnUtc: task.startOnUtc,
                endOnUtc: task.endOnUtc,
                commands: JsonOutput.toJson(task.dsl),
                status: task.status,
                failureReason: task.failureReason,
                hookUrl: task.hookUrl,
                docsUrl: task.docsUrl,
                hasUserInteraction: userInteraction != null,
        )
    }

    static scraper.dto.model.task.PendingTaskDto toPendingTaskDto(scraper.model.Task task) {
        def userInteraction = task.dsl?.settings?.userInteraction
        return new scraper.dto.model.task.PendingTaskDto(
                id: task.id,
                pipelineId: task.pipelineId,
                userId: task.userId,
                createdOnUtc: task.createdOnUtc,
                modifiedOnUtc: task.modifiedOnUtc,
                startOnUtc: task.startOnUtc,
                endOnUtc: task.endOnUtc,
                commands: JsonOutput.toJson(task.dsl),
                status: task.status,
                failureReason: task.failureReason,
                hookUrl: task.hookUrl,
                docsCount: task.docsCount,
                docsBytes: task.docsBytes,
                docsUrl: task.docsUrl,
                hasUserInteraction: userInteraction != null,
        )
    }

    static scraper.dto.model.task.TaskDto toTaskDto(scraper.model.Task task) {
        def userInteraction = task.dsl?.settings?.userInteraction
        return new scraper.dto.model.task.TaskDto(
                id: task.id,
                pipelineId: task.pipelineId,
                userId: task.userId,
                createdOnUtc: task.createdOnUtc,
                modifiedOnUtc: task.modifiedOnUtc,
                startOnUtc: task.startOnUtc,
                endOnUtc: task.endOnUtc,
                status: task.status,
                failureReason: task.failureReason,
                hookUrl: task.hookUrl,
                docsUrl: task.docsUrl,
                docsBytes: task.docsBytes,
                docsCount: task.docsCount,
                commandsInformationLink: task.commandsInformationLink,
                pipelineVersion: task.dsl?.version,
                hasUserInteraction: userInteraction != null,
        )
    }

    static TaskEditDto toTaskEditDto(scraper.model.Task task) {
        def userInteraction = task.dsl?.settings?.userInteraction
        return new TaskEditDto(
                id: task.id,
                pipelineId: task.pipelineId,
                userId: task.userId,
                createdOnUtc: task.createdOnUtc,
                modifiedOnUtc: task.modifiedOnUtc,
                startOnUtc: task.startOnUtc,
                endOnUtc: task.endOnUtc,
                status: task.status,
                failureReason: task.failureReason,
                hookUrl: task.hookUrl,
                docsUrl: task.docsUrl,
                docsBytes: task.docsBytes,
                docsCount: task.docsCount,
                commands: JsonOutput.toJson(task.dsl),
                commandsInformationLink: task.commandsInformationLink,
                hasUserInteraction: userInteraction != null,
        )
    }
}
