package scraper.service.controller

import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/site")
class SiteController {

    @RequestMapping("/get")
    String allowInFrame(@RequestParam String siteUrl) {
        CloseableHttpClient client = HttpClients.createDefault()
        HttpGet request = new HttpGet(siteUrl)
        HttpResponse response = client.execute(request)
        String frameOptions = response.getFirstHeader('X-Frame-Options')
        return frameOptions == null
    }
}
