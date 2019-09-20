package scraper.service.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.service.model.User
import scraper.service.repository.UserRepository

@Component
class UserService {

    @Autowired
    UserRepository userRepository

    User findById(String id) {
        Optional<User> user = userRepository.findById(id)
        return user.present ? user : null
    }

}
