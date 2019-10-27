package scraper.service.dto.model.pipeline

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import scraper.service.model.PipelineDependency

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class PipelineDto {
    String id
    String key
    String description
    Boolean isTakeScreenshot
    Boolean isDebugMode
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
    Number lastParseRowsCount
}