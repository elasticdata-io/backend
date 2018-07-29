package scraper.service.proxy

import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ProxyService {

    @Value('${proxy.service.url}')
    String proxyServiceUrl

    int CONNECT_TIMEOUT = 3 * 1000
    int READ_TIMEOUT = 3 * 1000

    ProxyModel getFastProxy() {
        def data = new URL(proxyServiceUrl).getText(connectTimeout: CONNECT_TIMEOUT, readTimeout: READ_TIMEOUT)
        if (!data) {
            return null
        }
        def jsonSlurper = new JsonSlurper()
        def object = jsonSlurper.parseText(data)
        return object as ProxyModel
    }
}
