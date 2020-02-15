package scraper.service.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/system")
class SystemController {

    @Value('${app.version}')
    private String APP_VERSION

    @Value('${app.last_updated}')
    private String APP_LAST_UPDATED

    @GetMapping("/version")
    String getVersion() {
       return APP_VERSION
    }

    @GetMapping("/last-updated")
    String getLastUpdated() {
       return APP_LAST_UPDATED
    }
}
