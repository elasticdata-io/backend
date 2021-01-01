package scraper.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import scraper.dto.mapper.UserMapper
import scraper.dto.model.user.UserDto
import scraper.model.User

@RestController
@RequestMapping("/users")
class UsersController {

    @Autowired
    scraper.service.UserService userService

    @Autowired
    scraper.repository.UserRepository userRepository

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
