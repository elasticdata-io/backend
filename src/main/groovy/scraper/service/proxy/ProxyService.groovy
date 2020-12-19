package scraper.service.proxy

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ProxyService {

    private Logger logger = LogManager.getRootLogger()

    @Value('${proxy.static.url}')
    String proxyStaticUrl

    String getStaticHttpProxy() {
        return proxyStaticUrl
    }
}
