package scraper.service.dto.model.task

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class UserInteractionDto {
    String id
    String jpegScreenshotLink
    List<HashMap> pageElements
    String currentUrl
    String pageContext
    String taskId
    String userId
    String pipelineId
    String status
    String pipelineStatus
    Date createdOnUtc
    Date modifiedOnUtc
    Date endOnUtc
    Number pageWidthPx
    Number pageHeightPx
    Date expiredOnUtc
}
