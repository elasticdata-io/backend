package scraper.service.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "pipeline")
class Pipeline {

    @Id
    public String id;

    /**
     * Pipeline key name.
     */
    public String key;

    /**
     * JSON of the pipeline process.
     */
    public String jsonCommands;

    /**
     * Path oth the pipeline JSON file, relative of the resources.
     */
    public String jsonCommandsPath;

    /**
     * Browser executor (chrome, phantom, etc.).
     */
    public String browser;

    @DBRef
    public User user;
}
