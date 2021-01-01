package scraper.dto.model.pipeline

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class DslDto {
    String version = '2.0'
    SettingsDslDto settings = new SettingsDslDto()
    List<Object> dataRules = []
    List<HashMap> commands = []
}
