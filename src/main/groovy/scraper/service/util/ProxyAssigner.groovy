package scraper.service.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.service.proxy.ProxyService

@Component
class ProxyAssigner {

    @Autowired
    ProxyService proxyService

    String getProxy() {
        def proxy = proxyService.getFastProxy()
        if (!proxy) {
            return null
        }
        return "${proxy.host}:${proxy.port}"
    }
}
