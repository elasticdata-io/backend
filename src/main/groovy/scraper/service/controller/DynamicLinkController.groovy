package scraper.service.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import scraper.service.dto.model.DynamicLinkDto
import scraper.service.service.DynamicLinkService

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("dynamic-link")
class DynamicLinkController {

    @Autowired
    DynamicLinkService dynamicLinkService

    @PostMapping('/create')
    String create(@RequestBody DynamicLinkDto dto, HttpServletRequest request) {
        return dynamicLinkService.create(dto, request)
    }

    @GetMapping('/to/{alias}')
    void get(@PathVariable String alias, HttpServletResponse httpServletResponse) {
        String url = dynamicLinkService.getRedirectUrl(alias)
        httpServletResponse.setHeader('Location', url)
        httpServletResponse.setStatus(302)
    }
}
