package scraper.service.dto.model.pipeline

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class TaskCommandExecuteDto {
    String commandExecutingProperties
    String commandExecutingName
    String pipelineId
    String pipelineTaskId
    String userId
}