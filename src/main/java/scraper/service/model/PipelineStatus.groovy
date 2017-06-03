package scraper.service.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "pipeline-status")
class PipelineStatus {

    @Id
    public String id;

    /**
     * Pipeline status name.
     */
    public String title;
}
