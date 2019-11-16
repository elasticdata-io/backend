package scraper.service.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.IndexDirection
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "task")
@CompoundIndex(def = "{'userId':1, 'status':-1}", name = "user_id__status")
class Task {

    @Id
    public String id
    @Version
    Long version

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    @Indexed(name = "start_on_utc_index", direction = IndexDirection.DESCENDING)
    public Date startOnUtc

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    public Date endOnUtc

    @Indexed(name = "pipeline_id_index", direction = IndexDirection.DESCENDING)
    public String pipelineId

    @Indexed(name = "user_id_index", direction = IndexDirection.DESCENDING)
    public String userId

    public String commands
    public String status
    public String failureReason
    public String hookUrl
    public String docsUrl

    /**
     * Указатель на родителя
     */
    public String parentTaskId

    public List<TaskDependency> taskDependencies
}
