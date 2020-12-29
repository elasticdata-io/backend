package scraper.service.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import scraper.service.dto.model.telegram.IncomingHookDto
import scraper.service.service.TelegramIncomingHookService

@RestController
@RequestMapping("telegram")
class TelegramController {

    @Autowired
    TelegramIncomingHookService telegramIncomingHookService

    @PostMapping('/botwebhook')
    void create(@RequestBody IncomingHookDto dto) {
        telegramIncomingHookService.handleIncomingHook(dto)
    }
}
