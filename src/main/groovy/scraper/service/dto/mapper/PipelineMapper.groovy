package scraper.service.dto.mapper

import org.springframework.stereotype.Component
import scraper.service.dto.model.pipeline.PipelineDto
import scraper.service.model.Pipeline

@Component
class PipelineMapper {

    static PipelineDto toPipelineDto(Pipeline pipeline) {
        if (!pipeline) {
            return null
        }
        return new PipelineDto(
                id: pipeline.id,
                key: pipeline.key,
                description: pipeline.description,
                isTakeScreenshot: pipeline.isTakeScreenshot,
                isDebugMode: pipeline.isDebugMode,
                needProxy: pipeline.needProxy,
                tasksTotal: pipeline.tasksTotal,
                createdOn: pipeline.createdOn,
                modifiedOn: pipeline.modifiedOn,
                lastStartedOn: pipeline.lastStartedOn,
                lastCompletedOn: pipeline.lastCompletedOn,
                status: PipelineStatusMapper.toPipelineStatusDto(pipeline.status),
                dependOn: toPipelineDto(pipeline.dependOn),
                userId: pipeline.user.id,
                jsonCommands: pipeline.jsonCommands,
                lastParseRowsCount: pipeline.parseRowsCount,
        )
    }

}
