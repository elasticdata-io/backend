package scraper.model.types

import com.fasterxml.jackson.annotation.JsonInclude

class SettingsDsl {
    /**
     * Max working pipeline in seconds
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Number maxWorkingMinutes

    /**
     * Browser window configuration
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    BrowserWindowDsl window

    /**
     * True if pipeline need proxy rotation,
     * If pipeline:true and proxies configured too -> rotation with user custom proxies list.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Boolean needProxyRotation

    /**
     * Browser proxies
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String[] proxies

    /**
     * User interaction configuration
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    UserInteractionDslSettings userInteraction

    @JsonInclude(JsonInclude.Include.NON_NULL)
    NetworkDsl network
}
