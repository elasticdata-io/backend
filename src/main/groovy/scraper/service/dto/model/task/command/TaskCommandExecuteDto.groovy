package scraper.service.dto.model.task.command

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class TaskCommandExecuteDto {
    String runTimeProperties
    String cmd
    String pipelineId
    String taskId
    String userId
}