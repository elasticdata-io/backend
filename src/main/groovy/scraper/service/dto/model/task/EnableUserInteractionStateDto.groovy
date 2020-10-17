package scraper.service.dto.model.task

class EnableUserInteractionStateDto {
    String jpegScreenshotLink
    List<HashMap> pageElements
    String currentUrl
    Number pageContext
    String taskId
    String userId
    String pipelineId
}
