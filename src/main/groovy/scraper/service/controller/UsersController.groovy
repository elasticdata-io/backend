package scraper.service.controller

import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import scraper.service.dto.mapper.UserMapper
import scraper.service.dto.model.user.UserDto
import scraper.service.model.User
import scraper.service.repository.UserRepository
import scraper.service.service.UserService

@RestController
@RequestMapping("/users")
class UsersController {

    private static Logger logger = LogManager.getLogger(UsersController.class)

    @Autowired
    UserService userService

    @Autowired
    UserRepository userRepository

    @GetMapping
    List<UserDto> list(@RequestHeader("token") String token) {
        def user = userService.findByToken(token)
        if (!user) {
            return
        }
        if (!user.isAdmin) {
            return
        }
        List<User> users = userRepository.findAll()
        def list = new ArrayList()
        users.each { u ->
            UserDto userDto = UserMapper.toUserDto(u)
            list.add(userDto)
        }
        return list
    }
}
