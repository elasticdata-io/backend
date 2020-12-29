package scraper.service.dto.model.telegram

class IncomingHookMessage {
    HashMap chat
    String text
}

class IncomingHookDto {
    IncomingHookMessage message
}
