package scraper.service.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "pipeline")
class Pipeline {

    @Id
    public String id;

    public String json;

    @DBRef
    public User user;

    public Date startOn;

    public Date endOn;
}
