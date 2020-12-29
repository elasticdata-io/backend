package scraper.service.dto.model.telegram

class IncomingHookChat {
    String id
}

class IncomingHookMessage {
    IncomingHookChat chat
    String text
}

class IncomingHookDto {
    IncomingHookMessage message
}
