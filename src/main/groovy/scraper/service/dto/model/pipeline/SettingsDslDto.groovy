package scraper.service.dto.model.pipeline

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class SettingsDslDto {
    /**
     * Browser window configuration
     */
    BrowserWindowDslDto window = new BrowserWindowDslDto()
    Boolean needProxyRotation = false
    /**
     * Browser proxies
     */
    String[] proxies = []
    NetworkDslDto network = new NetworkDslDto()
    /**
     * User interaction configuration
     */
    UserInteractionDsl userInteraction = new UserInteractionDsl()
    /**
     * Max working pipeline in seconds
     */
    Number maxWorkingMinutes = 1440
}
