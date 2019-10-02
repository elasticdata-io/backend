package scraper.service.dto.model.user

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

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
    PipelineStatusDto status
    PipelineDto dependOn
    String userId
    String jsonCommands
    Number lastParseRowsCount
}