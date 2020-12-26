package scraper.service.dto.model.pipeline

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class SettingsDslDto {
    /**
     * Max working pipeline in seconds
     */
    Number maxWorkingMinutes = 1440

    /**
     * Browser window configuration
     */
    BrowserWindowDslDto window = new BrowserWindowDslDto()

    /**
     * Browser proxies
     */
    String[] proxies = []

    /**
     * User interaction configuration
     */
    UserInteractionDsl userInteraction = new UserInteractionDsl()

    Boolean needProxyRotation = false

    NetworkDslDto network = new NetworkDslDto()
}
