package scraper.service.dto.model.task

class EnableUserInteractionModeDto {
    String jpegScreenshotLink
    Number pageWidthPx
    Number pageHeightPx
    List<HashMap> pageElements
    String currentUrl
    String pageContext
    String taskId
    String userId
    String pipelineId
}
