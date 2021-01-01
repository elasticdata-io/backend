package scraper.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import scraper.auth.TokenService
import scraper.dto.mapper.UserMapper
import scraper.dto.model.user.UserDto

import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/user")
class UserController {

    @Autowired
    private scraper.service.UserService userService

    @Autowired
    private TokenService tokenService

    @GetMapping("/current")
    UserDto current(@RequestHeader("token") String token) {
        def user = userService.findByToken(token)
        def userDto = UserMapper.toUserDto(user)
        return userDto
    }

    @GetMapping('/token/{userId}')
    String loginByUser(@PathVariable String userId, @RequestHeader("token") String token, HttpServletRequest request) {
        def user = userService.findByToken(token)
        if (!user) {
            return
        }
        if (!user.isAdmin) {
            return
        }
        def needUser = userService.findById(userId)
        if (!needUser) {
            return
        }
        String generatedToken = tokenService.makeToken(needUser.login)
        tokenService.saveUserToken(generatedToken, request)
        return generatedToken
    }
}
