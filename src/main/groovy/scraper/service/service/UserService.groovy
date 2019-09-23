package scraper.service.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.service.model.User
import scraper.service.repository.UserRepository

@Service
class UserService {

    @Autowired
    UserRepository userRepository

    User findById(String id) {
        Optional<User> user = userRepository.findById(id)
        return user.present ? user.get() : null
    }

}