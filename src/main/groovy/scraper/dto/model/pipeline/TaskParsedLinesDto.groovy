package scraper.dto.model.pipeline

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class TaskParsedLinesDto {
    Number newParseRowsCount
    String pipelineTaskId
    String pipelineId
    String userId
    Map<String, Object> line
}