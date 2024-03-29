package scraper.dto.model.pipeline

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class SkipResourcesDslDto {
    Boolean stylesheet = false
    Boolean image = false
    Boolean font = false
}
