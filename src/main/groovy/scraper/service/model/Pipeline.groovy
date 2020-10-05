package scraper.service.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import scraper.service.model.mapper.PipelineConfigurationMapper
import scraper.service.model.types.PipelineConfiguration
import scraper.service.model.types.PipelineSettings

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
    @Deprecated
    public String jsonCommandsPath

    /**
     * Version of pipeline syntax.
     */
    public String pipelineVersion

    /**
     * Settings of the pipeline configuration
     * (commands, dataRules, settings: need proxies, window size, language, max working time).
     */
    PipelineConfiguration getPipelineConfiguration() {
        return PipelineConfigurationMapper.toPipelineConfiguration(jsonCommands)
    }

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
     * Need proxy rotation for pipeline
     */
    public Boolean needProxy

    /**
     * Count of the all tasks of this pipeline.
     */
    Integer tasksTotal

    /**
     * Count of the last parsed data lines.
     */
    Number parseRowsCount

    Number parseBytes

    /**
     * The interval at which the parser will start.
     */
    Integer runIntervalMin

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
    public String status

    public String hookUrl

    /**
     * Depend on the pipeline.
     * @deprecated
     */
    @DBRef
    public Pipeline dependOn

    /**
     * Depend on the pipelines.
     */
    public List<PipelineDependency> dependencies

    @DBRef
    @JsonIgnore
    public User user

}
