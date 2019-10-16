package scraper.service.proxy

import groovy.json.JsonSlurper
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ProxyService {

    private Logger logger = LogManager.getRootLogger()

    @Value('${proxy.service.url}')
    String proxyServiceUrl

    int CONNECT_TIMEOUT = 3 * 1000
    int READ_TIMEOUT = 3 * 1000

    ProxyModel getFastProxy() {
        def data = new URL(proxyServiceUrl + "/proxy/fast")
                .getText(connectTimeout: CONNECT_TIMEOUT, readTimeout: READ_TIMEOUT)
        if (!data) {
            logger.error("proxy not found, proxyServiceUrl: ${proxyServiceUrl}")
            return null
        }
        def jsonSlurper = new JsonSlurper()
        def object = jsonSlurper.parseText(data)
        logger.info("proxy found, data: ${data}")
        return object as ProxyModel
    }
}
