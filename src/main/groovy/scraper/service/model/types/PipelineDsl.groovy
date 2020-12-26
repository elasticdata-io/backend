package scraper.service.model.types

import com.fasterxml.jackson.annotation.JsonInclude

class PipelineDsl {
    List<Object> commands

    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<Object> dataRules

    @JsonInclude(JsonInclude.Include.NON_NULL)
    SettingsDsl settings
    String version
}
