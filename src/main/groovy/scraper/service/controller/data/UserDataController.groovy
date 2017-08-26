package scraper.service.controller.data

import io.jsonwebtoken.Claims
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import scraper.service.controller.response.SimpleResponse
import scraper.service.model.Pipeline
import scraper.service.model.PipelineStatus
import scraper.service.model.User
import scraper.service.repository.UserRepository
import scraper.service.util.EmailValidator
import scraper.service.util.TokenService

import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/user")
class UserDataController {

    @Autowired
    UserRepository userRepository

    @Autowired
    TokenService tokenService

    @Autowired
    EmailValidator emailValidator

    @RequestMapping('/')
    User get(@RequestHeader("token") String token) {
        Claims claims = tokenService.parseToken(token)
        if (!claims) {
            return null
        }
        String login = claims.get(TokenService.LOGIN)
        def user = userRepository.findByLogin(login)
        return user
    }

    @RequestMapping('/save')
    SimpleResponse save(HttpServletRequest request, @RequestHeader String token) {
        User user = userRepository.findByToken(token)
        String login = request.getParameter('login')
        String email = request.getParameter('email')
        String firstName = request.getParameter('firstName')
        String secondName = request.getParameter('secondName')
        String lastName = request.getParameter('lastName')
        if (!login) {
            return new SimpleResponse(success: false, error: 'login is required field')
        }
        if (!email) {
            return new SimpleResponse(success: false, error: 'email is required field')
        }
        if (!emailValidator.validate(email)) {
            return new SimpleResponse(success: false, error: 'email is not valid value')
        }
        if (user && login != user.login) {
            User userByLogin = userRepository.findByLogin(login)
            if (userByLogin) {
                return new SimpleResponse(success: false, error: "Login '${login}' is busy")
            }
        }
        if (user) {
            user.firstName = firstName
            user.secondName = secondName
            user.lastName = lastName
            user.login = login
            user.email = email
            userRepository.save(user)
            def newToken = tokenService.makeToken(login)
            user.token = newToken
            userRepository.save(user)
            return new SimpleResponse(success: true, message: 'this user has been saved', token: newToken)
        }
        return new SimpleResponse(success: false, error: 'User not found')
    }

    @RequestMapping('/password/change')
    SimpleResponse changePassword(HttpServletRequest request, @RequestHeader String token) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder()
        User user = userRepository.findByToken(token)
        String password = request.getParameter('password')
        String passwordAgain = request.getParameter('passwordAgain')
        if (!password) {
            return new SimpleResponse(success: false, error: 'password is required field')
        }
        if (!passwordAgain) {
            return new SimpleResponse(success: false, error: 'passwordAgain is required field')
        }
        if (passwordAgain != password) {
            return new SimpleResponse(success: false, error: 'password and passwordAgain is not equal')
        }
        if (user) {
            user.password = passwordEncoder.encode("${password}${user.salt}")
            userRepository.save(user)
            return new SimpleResponse(success: true, message: 'password has been changed')
        }
        return new SimpleResponse(success: false, error: 'User not found')
    }
}
