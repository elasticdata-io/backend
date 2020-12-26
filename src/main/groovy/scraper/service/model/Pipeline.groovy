package scraper.service.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.json.JsonOutput
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import scraper.service.model.types.PipelineDsl

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
     * @deprecated use dsl prop
     */
    @Deprecated
    public String jsonCommands

    /**
     * Settings of the pipeline configuration (Settings DSL)
     * (commands, dataRules, settings: need proxies, window size, language, max working time).
     */
    public PipelineDsl dsl

    /**
     * Version of pipeline syntax.
     */
    public String pipelineVersion

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
     * @deprecated use dsl.settings.needProxyRotation
     */
    @Deprecated
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
    @CreatedDate
    public Date createdOn

    /**
     * Modified on date time.
     */
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    @LastModifiedDate
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

    void setDsl(PipelineDsl dsl) {
        this.jsonCommands = JsonOutput.toJson(dsl)
    }

}
