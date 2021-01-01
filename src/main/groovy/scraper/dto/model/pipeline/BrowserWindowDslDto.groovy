package scraper.dto.model.pipeline

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class BrowserWindowDslDto {
    Number width = 1800
    Number height = 1000
    /**
     * Browser language
     */
    String lang = 'en'
}
