package scraper.service.config.filter

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import scraper.service.util.TokenService

import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class TokenFilter extends GenericFilterBean {

    String TOKEN_HEADER_NAME = 'token'
    String LOGIN_URI = '/api/login'
    String WS_URI = '/ws/'

    String allowUrls = [LOGIN_URI, '/api/pipeline/data/', '/api/pipeline-task/data/', WS_URI]

    @Autowired
    TokenService tokenService

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletResponse httpResponse = response as HttpServletResponse
        HttpServletRequest httpRequest = request as HttpServletRequest
        String uri = httpRequest.getRequestURI()
        boolean isAllowUrl = allowUrls.find {url -> uri.startsWith(url)}
        if (isAllowUrl || checkToken(request)) {
            chain.doFilter(request, response)
            return
        }
        httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, 'Unauthorized')
    }

    private boolean checkToken(ServletRequest request) {
        HttpServletRequest httpRequest = request as HttpServletRequest
        String token = httpRequest.getHeader(TOKEN_HEADER_NAME) ?: httpRequest.getParameter(TOKEN_HEADER_NAME)
        return tokenService.checkToken(token)
    }
}
