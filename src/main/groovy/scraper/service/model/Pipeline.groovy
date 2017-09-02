package scraper.service.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "pipeline")
class Pipeline {

    @Id
    public String id

    /**
     * Pipeline key name.
     */
    public String key

    /**
     * JSON of the pipeline process.
     */
    public String jsonCommands

    /**
     * Path oth the pipeline JSON file, relative of the resources.
     */
    public String jsonCommandsPath

    /**
     * Browser executor (chrome, phantom, etc.).
     */
    public String browser

    /**
     * Browser ip or domain address.
     */
    public String browserAddress

    /**
     * Description of the pipeline.
     */
    public String description

    /**
     * Need to take screenshots.
     */
    public Boolean isTakeScreenshot

    /**
     * Need to save debug information.
     */
    public Boolean isDebugMode

    /**
     * Count of the last parsed data lines.
     */
    Integer lastParsedLinesCount

    /**
     * Created on date time.
     */
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    public Date createdOn

    /**
     * Modified on date time.
     */
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    public Date modifiedOn

    /**
     * Last started on date time.
     */
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    public Date lastStartedOn

    /**
     * Last completed on date time.
     */
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    public Date lastCompletedOn

    /**
     * Status of the pipeline (stop|run|etc).
     */
    @DBRef
    public PipelineStatus status

    /**
     * Depend on the pipeline.
     */
    @DBRef
    public Pipeline dependOn

    @DBRef
    @JsonIgnore
    public User user

}
