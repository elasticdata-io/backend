package scraper.service.proxy

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import scraper.service.proxy.ProxyService

@Component
class ProxyAssigner {

    private Logger logger = LogManager.getRootLogger()

    @Autowired
    ProxyService proxyService

    @Value('${proxy.tor}')
    String proxyTor

    String getProxy() {
        logger.info("proxy tor: ${proxyTor}")
        return proxyTor
    }
}
