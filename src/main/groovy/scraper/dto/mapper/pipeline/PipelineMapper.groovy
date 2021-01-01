package scraper.dto.mapper.pipeline

import org.springframework.stereotype.Component
import scraper.dto.model.pipeline.PipelineDto

@Component
class PipelineMapper {

    static scraper.model.Pipeline toPipeline(PipelineDto pipelineDto) {
        if (!pipelineDto) {
            return null
        }
        return new scraper.model.Pipeline(
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
            dsl: DslMapper.toDslEntity(pipelineDto.dsl),
            hookUrl: pipelineDto.hookUrl,
        )
    }

    static PipelineDto toPipelineDto(scraper.model.Pipeline pipeline) {
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
                dsl: DslMapper.toPipelineDslDto(pipeline.dsl),
                lastParseRowsCount: pipeline.parseRowsCount,
                lastParseBytes: pipeline.parseBytes,
                pipelineVersion: pipeline.pipelineVersion,
                hookUrl: pipeline.hookUrl,
        )
    }

}
