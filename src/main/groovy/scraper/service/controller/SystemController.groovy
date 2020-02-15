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


    @GetMapping("/version")
    String getVersion() {
       return APP_VERSION
    }
}
