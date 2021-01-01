package scraper.config.oauth2;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Oauth2UrlAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * Redirect to custom make token and response to client.
     * Redirect to Login controller.
     * @param request
     * @param response
     * @param authentication
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String redirect = request.getRequestURL().toString();
        response.sendRedirect(redirect);
    }
}
