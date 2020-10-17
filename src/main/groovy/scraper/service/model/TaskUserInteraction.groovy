package scraper.service.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.IndexDirection
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "task-user-interaction")
class TaskUserInteraction {

    @Id
    public String id

    @Version
    Long version

    @Indexed(name = "pipeline_id_index", direction = IndexDirection.DESCENDING)
    public String pipelineId

    @Indexed(name = "user_id_index", direction = IndexDirection.DESCENDING)
    public String userId

    @Indexed(name = "task_id_index", direction = IndexDirection.DESCENDING)
    public String taskId

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    @Indexed(name = "start_on_utc_index", direction = IndexDirection.DESCENDING)
    public Date createdOnUtc

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    @Indexed(name = "modified_on_utc_index", direction = IndexDirection.DESCENDING)
    public Date modifiedOnUtc

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    public Date endOnUtc

    @Indexed(name = "pipeline_status_index", direction = IndexDirection.DESCENDING)
    public String pipelineStatus

    @Indexed(name = "status_index", direction = IndexDirection.DESCENDING)
    public String status

    @Indexed(name = "page_context_index", direction = IndexDirection.DESCENDING)
    public String pageContext

    /**
     * Screenshot link, pageElements, window size, page context, current url and etc.
     */
    public HashMap<String, Object> lastPageState
}
