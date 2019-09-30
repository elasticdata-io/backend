package scraper.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilterHandlerInterceptor extends HandlerInterceptorAdapter {

    private static final String TOKEN_HEADER_NAME = "token";

    @Autowired
    private TokenService tokenService;

    private boolean filter(ServletRequest request, ServletResponse response) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse)response;
        if (this.checkToken(request)) {
            return true;
        }
        httpResponse.setStatus(401);
        httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        return false;
    }

    private boolean checkToken(ServletRequest request) {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        String tokenFromHeader = httpRequest.getHeader(TOKEN_HEADER_NAME);
        String token = tokenFromHeader != null ? tokenFromHeader : httpRequest.getParameter(TOKEN_HEADER_NAME);
        return tokenService.checkToken(token);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            OnlyAuth onlyAuthAnnotation = handlerMethod.getMethodAnnotation(OnlyAuth.class);
            if (onlyAuthAnnotation == null) {
                return true;
            }
            return this.filter(request, response);
        }
        return true;
    }
}
