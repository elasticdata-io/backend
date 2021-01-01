package scraper.dto.mapper

import groovy.json.JsonOutput
import org.springframework.stereotype.Component
import scraper.dto.model.task.HookTaskDto
import scraper.dto.model.task.PendingApiTaskDto
import scraper.dto.model.task.PendingTaskDto
import scraper.dto.model.task.TaskDto
import scraper.dto.model.task.TaskEditDto
import scraper.model.Task

@Component
class TaskMapper {

    static HookTaskDto toHookTaskDto(Task task) {
        return new HookTaskDto(
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

    static PendingApiTaskDto toPendingApiTaskDto(Task task) {
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

    static PendingTaskDto toPendingTaskDto(Task task) {
        def userInteraction = task.dsl?.settings?.userInteraction
        return new PendingTaskDto(
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

    static TaskDto toTaskDto(Task task) {
        def userInteraction = task.dsl?.settings?.userInteraction
        return new TaskDto(
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

    static TaskEditDto toTaskEditDto(Task task) {
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
