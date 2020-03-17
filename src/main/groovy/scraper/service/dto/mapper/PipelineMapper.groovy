package scraper.service.dto.mapper

import org.springframework.stereotype.Component
import scraper.service.dto.model.pipeline.PipelineDto
import scraper.service.model.Pipeline

@Component
class PipelineMapper {

    static Pipeline toPipeline(PipelineDto pipelineDto) {
        if (!pipelineDto) {
            return null
        }
        return new Pipeline(
            id: pipelineDto.id,
            key: pipelineDto.key,
            description: pipelineDto.description,
            isTakeScreenshot: pipelineDto.isTakeScreenshot,
            isDebugMode: pipelineDto.isDebugMode,
            needProxy: pipelineDto.needProxy,
            tasksTotal: pipelineDto.tasksTotal,
            createdOn: pipelineDto.createdOn,
            modifiedOn: pipelineDto.modifiedOn,
            lastStartedOn: pipelineDto.lastStartedOn,
            lastCompletedOn: pipelineDto.lastCompletedOn,
            status: pipelineDto.status,
            dependencies: PipelineDependencyMapper.toPipelineDependencies(pipelineDto.dependencies),
            jsonCommands: pipelineDto.jsonCommands,
        )
    }

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
                status: pipeline.status,
                dependencies: PipelineDependencyMapper.toPipelineDependenciesDto(pipeline.dependencies),
                userId: pipeline.user.id,
                jsonCommands: pipeline.jsonCommands,
                lastParseRowsCount: pipeline.parseRowsCount,
                lastParseBytes: pipeline.parseBytes,
        )
    }

}
