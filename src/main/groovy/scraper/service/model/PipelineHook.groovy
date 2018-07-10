package scraper.service.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "pipeline-hook")
class PipelineHook {

    @Id
    public String id

    @DBRef
    public Pipeline pipeline

    public String hookUrl

    public String jsonConfig
}
