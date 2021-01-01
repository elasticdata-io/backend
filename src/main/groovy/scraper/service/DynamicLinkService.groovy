package scraper.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import scraper.model.DynamicLink
import scraper.repository.DynamicLinkRepository

import javax.servlet.http.HttpServletRequest

@Service
class DynamicLinkService {

    @Autowired
    DynamicLinkRepository dynamicLinkRepository

    @Value('${server.servlet.context-path}')
    String contextPath

    String create(String absoluteUrl, HttpServletRequest request) {
        if (!absoluteUrl) {
            return absoluteUrl
        }
        def model = dynamicLinkRepository.findByAbsoluteUrl(absoluteUrl)
        String alias = generateAlias()
        if (model) {
            alias = model.alias
        }
        DynamicLink dynamicLink = new DynamicLink(
            alias: alias,
            absoluteUrl: absoluteUrl,
            createdOn: new Date(),
        )
        dynamicLinkRepository.save(dynamicLink)
        int port = request.getServerPort()
        String portIn = port == 80 || port == 443 ? '' : ":${port}"
        String baseUrl = "${request.getScheme()}://${request.getServerName()}${portIn}${contextPath}"
        return "${baseUrl}/link/to/${alias}"
    }

    String getRedirectUrl(String alias) {
        def dynamicLink = dynamicLinkRepository.findByAlias(alias)
        return "${dynamicLink.absoluteUrl}"
    }

    private static String generateAlias() {
        def generator = { String alphabet, int n ->
            new Random().with {
                (1..n).collect { alphabet[ nextInt( alphabet.length() ) ] }.join()
            }
        }
        return generator( (('a'..'z')+('0'..'9')).join(), 12)
    }
}