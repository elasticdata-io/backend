package scraper.service.controller

import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import scraper.service.dto.mapper.UserMapper
import scraper.service.dto.model.user.UserDto
import scraper.service.model.UserToken
import scraper.service.service.UserService
import scraper.service.service.UserTokenService

@RestController
@RequestMapping("/user")
class UserController {

    private static Logger logger = LogManager.getLogger(UserController.class)

    @Autowired
    private UserService userService

    @Autowired
    private UserTokenService userTokenService

    @GetMapping("/current")
    UserDto current(@RequestHeader("token") String token) {
        def user = userService.findByToken(token)
        def userDto = UserMapper.toUserDto(user)
        return userDto
    }

    @GetMapping('/token/{userId}')
    String loginByUser(@PathVariable String userId, @RequestHeader("token") String token) {
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
        UserToken userToken = userTokenService.findLastToken(needUser)
        if (!userToken) {
            return
        }
        return userToken.token
    }
}
