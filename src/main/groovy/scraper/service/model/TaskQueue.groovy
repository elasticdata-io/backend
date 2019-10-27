package scraper.service.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "task-queue")
class TaskQueue {
    @Id
    public String id
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    public Date createdOnUtc
    public String pipelineId
    public String userId
    public String taskId
    /**
     * TaskQueueStatuses
     */
    public String status
}
