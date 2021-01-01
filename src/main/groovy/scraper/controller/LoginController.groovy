package scraper.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import scraper.dto.SimpleResponse
import scraper.model.User
import scraper.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import scraper.auth.TokenService
import scraper.service.UserService

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

@RestController
@RequestMapping("/login")
class LoginController {

    @Value('${security.googleSignInCallbackUrl}')
    private String googleSignInCallbackUrl

    @Value('${security.facebookSignInCallbackUrl}')
    private String facebookSignInCallbackUrl

    @Autowired
    private UserService userService

    @Autowired
    private UserRepository userRepository

    @Autowired
    private TokenService tokenService

    @RequestMapping()
    SimpleResponse login(@RequestParam String login, @RequestParam String password, HttpServletRequest request)
            throws Exception {
        User user = userRepository.findByLogin(login)
        String msg = String.format("Пользователь c логином '%s', или паролем, не найден.", login)
        if (user == null) {
            return new SimpleResponse(success: false, error: msg)
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder()
        String passwordEncoded = passwordEncoder.encode(password + user.salt)
        println passwordEncoded
        if (!passwordEncoder.matches(password + user.salt, user.password)) {
            return new SimpleResponse(success: false, error: msg)
        }
        String token = tokenService.makeToken(login, password)
        tokenService.saveUserToken(token, request)
        String message = 'Вы успешно авторизированы, переадресация!'
        return new SimpleResponse(success: true, message: message, token: token, userId: user.id)
    }

    @GetMapping("/exit")
    void logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext()
        HttpSession session = request.getSession(false)
        if(session != null) {
            session.invalidate()
        }
        for(Cookie cookie : request.getCookies()) {
            cookie.setMaxAge(0)
        }
    }

    @GetMapping("/oauth2/code/google")
    RedirectView googleLogIn(OAuth2AuthenticationToken authentication, HttpServletRequest request) {
        User user = userService.createOrUpdateFromGoogle(authentication.getPrincipal())
        RedirectView redirectView = new RedirectView()
        String token = tokenService.makeToken(user.login)
        tokenService.saveUserToken(token, request)
        def redirectUrl = googleSignInCallbackUrl + "${user.id}?token=${token}"
        redirectView.setUrl(redirectUrl)
        return redirectView
    }

    @GetMapping("/oauth2/code/facebook")
    RedirectView facebookLogIn(OAuth2AuthenticationToken authentication, HttpServletRequest request) {
        User user = userService.createOrUpdateFromFacebook(authentication.getPrincipal())
        RedirectView redirectView = new RedirectView()
        String token = tokenService.makeToken(user.login)
        tokenService.saveUserToken(token, request)
        def redirectUrl = facebookSignInCallbackUrl + "${user.id}?token=${token}"
        redirectView.setUrl(redirectUrl)
        return redirectView
    }

    @GetMapping("/is-auth")
    Boolean isAuth(@RequestHeader("token") String token) {
        boolean valid = tokenService.checkToken(token)
        return valid
    }

}