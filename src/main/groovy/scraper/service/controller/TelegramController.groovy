package scraper.service.controller

import groovy.json.JsonOutput
import org.springframework.web.bind.annotation.*
import scraper.service.dto.model.telegram.IncomingHookDto

@RestController
@RequestMapping("telegram")
class TelegramController {

    @PostMapping('/botwebhook')
    void create(@RequestBody IncomingHookDto dto) {
        println JsonOutput.toJson(dto)
    }
}
