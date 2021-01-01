package scraper.dto.model.task.command

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class TaskCommandExecuteDto {
    Object designTimeConfig
    String cmd
    String pipelineId
    String taskId
    String userId
    String uuid
}