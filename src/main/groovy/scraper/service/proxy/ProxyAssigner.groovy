package scraper.service.proxy

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ProxyAssigner {

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    ProxyService proxyService


    String getProxy() {
        ProxyModel proxy = proxyService.getFastProxy()
        return "${proxy.host}:${proxy.port}"
    }
}
