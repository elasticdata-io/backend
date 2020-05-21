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
        String url = proxyService.getStaticHttpProxy()
        logger.info("proxy: ${url}")
        return url
        /*
        ProxyModel proxy = proxyService.getFastProxy()
        String url
        if (proxy.type == 'socks5') {
            url = "socks5://${proxy.host}:${proxy.port}"
        } else {
            url = "${proxy.host}:${proxy.port}"
        }
        logger.info("proxy url: ${url}")
        return url
        */
    }
}
