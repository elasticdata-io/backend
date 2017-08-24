package scraper.service.controller

import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import scraper.service.controller.response.SimpleResponse
import scraper.service.model.User
import scraper.service.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import scraper.service.util.TokenService

import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api/login")
class LoginController {

    private static Logger logger = LogManager.getLogger(LoginController.class)

    @Autowired
    private UserRepository userRepository

    @Autowired
    private TokenService tokenService

    @RequestMapping
    SimpleResponse login(@RequestParam String login, @RequestParam String password, HttpServletResponse response)
            throws Exception {
        User user = userRepository.findByLogin(login)
        String msg = String.format("Пользователь c логином '%s', или паролем, не найден.", login)
        if (user == null) {
            return new SimpleResponse(success: false, error: msg)
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder()
        if (!passwordEncoder.matches(password + user.salt, user.password)) {
            return new SimpleResponse(success: false, error: msg)
        }
        String token = tokenService.makeToken(login, password)
        tokenService.registerTokenToCookie(token, response)
        user.token = token
        userRepository.save(user)
        String message = 'Вы успешно авторизированы, переадресация!'
        return new SimpleResponse(success: true, message: message, token: token)
    }
}
