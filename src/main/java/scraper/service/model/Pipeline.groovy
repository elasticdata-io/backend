package scraper.service.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "pipeline")
class Pipeline {

    @Id
    public String id;

    public String key;

    public String jsonCommands;

    @DBRef
    public User user;
}
