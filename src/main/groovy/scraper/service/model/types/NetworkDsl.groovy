package scraper.service.model.types

class SkipResourcesDsl {
    Boolean stylesheet
    Boolean image
}

class NetworkDsl {
    SkipResourcesDsl skipResources
}
