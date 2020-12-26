package scraper.service.dto.model.task

class HookTaskDto {
    String taskId
    String pipelineId
    String userId
    Date createdOnUtc
    Date modifiedOnUtc
    Date startOnUtc
    Date endOnUtc
    String status
    String failureReason
    String hookUrl
    String docsUrl
    String pipelineVersion
    Number docsCount
    Number docsBytes
}
