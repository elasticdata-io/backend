package scraper.service.dto.model.pipeline

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class DslDto {
    String version = 'v2'
    SettingsDslDto settings = new SettingsDslDto()
    List<DataRuleDslDto> dataRules = []
    List<HashMap> commands = []
}
