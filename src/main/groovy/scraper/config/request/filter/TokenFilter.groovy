package scraper.config.request.filter

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import scraper.service.auth.ApiTokenService
import scraper.service.auth.JwtTokenService

import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class TokenFilter extends GenericFilterBean {

    String TOKEN_HEADER_NAME = 'token'
    String API_PARAMETER_NAME = 'API_KEY'

    String SWAGGER_API = '/api/v3/api-docs'
    String SWAGGER_UI = '/api/swagger-ui'
    String LOGIN_URI = '/api/login'
    String WS_URI = '/api/ws'

    List<String> allowUrls = [
            SWAGGER_API,
            SWAGGER_UI,
            WS_URI,
            LOGIN_URI,
            '/api/pipeline/data',
            '/api/pipeline/task/synchronize',
            '/api/pipeline-task/data',
            '/api/task',
            '/api/pipeline/run',
            '/api/actuator/health',
            '/api/liveness',
            '/api/test',
            '/api/system',
            '/api/link',
    ]

    @Autowired
    JwtTokenService jwtTokenService

    @Autowired
    ApiTokenService apiTokenService

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletResponse httpResponse = response as HttpServletResponse
        HttpServletRequest httpRequest = request as HttpServletRequest
        String uri = httpRequest.getRequestURI()
        boolean isAllowUrl = allowUrls.find { uri.startsWith(it) }
        if (isAllowUrl) {
            chain.doFilter(request, response)
            return
        }
        if (checkJwtToken(request)) {
            chain.doFilter(request, response)
            return
        }
        if (checkApiKey(request)) {
            chain.doFilter(request, response)
            return
        }
        httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, 'Unauthorized')
    }

    private boolean checkJwtToken(ServletRequest request) {
        HttpServletRequest httpRequest = request as HttpServletRequest
        String token = httpRequest.getHeader(TOKEN_HEADER_NAME) ?: httpRequest.getParameter(TOKEN_HEADER_NAME)
        return jwtTokenService.checkToken(token)
    }

    private boolean checkApiKey(ServletRequest request) {
        HttpServletRequest httpRequest = request as HttpServletRequest
        String token = httpRequest.getHeader(API_PARAMETER_NAME) ?: httpRequest.getParameter(API_PARAMETER_NAME)
        return apiTokenService.checkToken(token)
    }
}
