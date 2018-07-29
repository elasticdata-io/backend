package scraper.service.util

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.service.proxy.ProxyService

@Component
class ProxyAssigner {

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    ProxyService proxyService

    String getProxy() {
        try {
            def proxy = proxyService.getFastProxy()
            if (!proxy) {
                return null
            }
            return "${proxy.host}:${proxy.port}"
        } catch (e) {
            logger.error(e)
            return null
        }
    }
}
