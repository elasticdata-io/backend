package scraper.service.model.types

class PipelineSettings {
    /**
     * Max working pipeline in seconds
     */
    Number maxWorkingMinutes

    /**
     * Browser window configuration
     */
    PipelineWindowSettings window

    /**
     * Browser proxies
     */
    String[] proxies
}
