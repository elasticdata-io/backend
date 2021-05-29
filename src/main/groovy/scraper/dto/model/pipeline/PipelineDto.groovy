package scraper.dto.model.pipeline

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import scraper.constants.WorkerTypes

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class PipelineDto {
    String id
    String key
    String description
    Boolean isTakeScreenshot
    Boolean isDebugMode
    /**
     * @deprecated use dsl.settings.needProxyRotation
     */
    @Deprecated
    Boolean needProxy
    Number tasksTotal
    Date createdOn
    Date modifiedOn
    Date lastStartedOn
    Date lastCompletedOn
    String status
    List<PipelineDependencyDto> dependencies
    String userId
    String jsonCommands
    DslDto dsl
    Number lastParseRowsCount
    Number lastParseBytes
    String pipelineVersion
    String hookUrl
    String assignWorkerType = WorkerTypes.SHARED
}