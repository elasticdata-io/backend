package scraper.service.model.types

class SettingsDsl {
    /**
     * Max working pipeline in seconds
     */
    Number maxWorkingMinutes

    /**
     * Browser window configuration
     */
    BrowserWindowDsl window

    /**
     * True if pipeline need proxy rotation,
     * If pipeline:true and proxies configured too -> rotation with user custom proxies list.
     */
    Boolean needProxyRotation

    /**
     * Browser proxies
     */
    String[] proxies

    /**
     * User interaction configuration
     */
    UserInteractionDslSettings userInteraction
}
