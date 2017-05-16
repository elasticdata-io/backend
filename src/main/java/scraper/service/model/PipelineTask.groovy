package scraper.service.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "pipeline-task")
class PipelineTask {

    @Id
    public String id;

    public String json;

    @DBRef
    public Pipeline pipeline;

    public Date startOn;

    public Date endOn;
}
