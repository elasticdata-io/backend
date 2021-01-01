package scraper.dto.model.task

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class UserInteractionStateDto {
    String interactionId
    String jpegScreenshotLink
    Number pageWidthPx
    Number pageHeightPx
    List<HashMap> pageElements
    String currentUrl
    String pageContext
    String taskId
    String userId
    String pipelineId
    Number timeoutSeconds
}
