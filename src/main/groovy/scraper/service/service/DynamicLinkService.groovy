package scraper.service.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import scraper.service.dto.model.DynamicLinkDto
import scraper.service.model.DynamicLink
import scraper.service.repository.DynamicLinkRepository

import javax.servlet.http.HttpServletRequest

@Service
class DynamicLinkService {

    @Autowired
    DynamicLinkRepository dynamicLinkRepository

    @Value('${server.servlet.context-path}')
    String contextPath

    String create(DynamicLinkDto dto, HttpServletRequest request) {
        def alias = UUID.randomUUID().toString()
        DynamicLink dynamicLink = new DynamicLink(
            alias: alias,
            absoluteUrl: dto.absoluteUrl,
            createdOn: new Date(),
        )
        dynamicLinkRepository.save(dynamicLink)
        String baseUrl = "${request.getScheme()}://${request.getServerName()}:${request.getServerPort()}${contextPath}";
        return "${baseUrl}/dynamic-link/to/${alias}"
    }

    String getRedirectUrl(String alias) {
        def dynamicLink = dynamicLinkRepository.findByAlias(alias)
        return "${dynamicLink.absoluteUrl}"
    }
}
