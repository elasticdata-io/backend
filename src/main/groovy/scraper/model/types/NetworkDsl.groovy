package scraper.model.types

class SkipResourcesDsl {
    Boolean stylesheet
    Boolean image
    Boolean font
}

class NetworkDsl {
    SkipResourcesDsl skipResources
}
