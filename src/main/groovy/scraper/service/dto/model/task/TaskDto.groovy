package scraper.service.dto.model.task

class TaskDto {
    String id
    String pipelineId
    String userId
    Date startOnUtc
    Date endOnUtc
    String status
    String failureReason
    String hookUrl
    String docsUrl
}
