package scraper.service.dto.model.pipeline

class SettingsDslDto {
    /**
     * Max working pipeline in seconds
     */
    Number maxWorkingMinutes

    /**
     * Browser window configuration
     */
    BrowserWindowDslDto window

    /**
     * Browser proxies
     */
    String[] proxies

    /**
     * User interaction configuration
     */
    UserInteractionDsl userInteraction

    Boolean needProxyRotation
}
