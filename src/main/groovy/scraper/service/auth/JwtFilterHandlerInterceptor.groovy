package scraper.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter

import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtFilterHandlerInterceptor extends HandlerInterceptorAdapter {

    String LOGIN_URI = '/api/login'
    String WS_URI = '/api/ws/'

    List<String> allowUrls = [WS_URI, LOGIN_URI, '/api/pipeline/data/', '/api/pipeline-task/data/', '/api/pipeline/run/']

    private static final String TOKEN_HEADER_NAME = "token"

    @Autowired
    private TokenService tokenService

    private boolean filter(ServletRequest request, ServletResponse response) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse)response

        HttpServletRequest httpRequest = request as HttpServletRequest
        String uri = httpRequest.getRequestURI()
        boolean isAllowUrl = allowUrls.find { uri.startsWith(it) }
        if (isAllowUrl || checkToken(request)) {
            return true
        }
        httpResponse.setStatus(401)
        httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
        return false
    }

    private boolean checkToken(ServletRequest request) {
        HttpServletRequest httpRequest = (HttpServletRequest)request
        String tokenFromHeader = httpRequest.getHeader(TOKEN_HEADER_NAME)
        String token = tokenFromHeader != null ? tokenFromHeader : httpRequest.getParameter(TOKEN_HEADER_NAME)
        return tokenService.checkToken(token)
    }

    @Override
    boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler
            OnlyAuth onlyAuthAnnotation = handlerMethod.getMethodAnnotation(OnlyAuth.class)
            if (onlyAuthAnnotation == null) {
                return true
            }
            return this.filter(request, response)
        }
        return true
    }
}
