package scraper.service.controller

import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import scraper.service.dto.model.mapper.UserMapper
import scraper.service.dto.model.user.UserDto
import scraper.service.service.UserService

@RestController
@RequestMapping("/user")
class UserController {

    private static Logger logger = LogManager.getLogger(UserController.class)

    @Autowired
    UserService userService

    @GetMapping("/current")
    UserDto current(@RequestHeader("token") String token) {
        def user = userService.findByToken(token)
        def userDto = UserMapper.toUserDto(user)
        return userDto
    }
}
