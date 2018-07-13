package scraper.service.util

import groovyx.net.http.HTTPBuilder
import org.springframework.stereotype.Component

@Component
class ProxyAssigner {

    String getProxy() {
        def http = new HTTPBuilder('http://list.didsoft.com')
        def query = [email : 'bombascter@gmail.com', pass: '!Prisoner31!', pid: 'httppremium']
        def html = http.get( path : '/get', query : query ) as StringReader
        String text = html.text
        def list = text.split("\n") as ArrayList<String>
        def last = list.sort().last()
        return normalize(last)
    }

    String normalize(String ip) {
        return ip.replaceAll("#.*", "")
    }
}
