package scraper.service.dto.model.task

import groovy.json.JsonOutput

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

    @Override String toString() {
        return JsonOutput.toJson([id: id, pipelineId: pipelineId, userId: userId, status: status])
    }
}
