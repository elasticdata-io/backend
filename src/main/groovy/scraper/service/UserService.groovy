package scraper.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import scraper.auth.TokenService
import scraper.constants.PipelineStatuses
import scraper.model.User
import scraper.repository.UserRepository

@Service
class UserService {

    @Autowired
    TokenService tokenService

    @Autowired
    UserRepository userRepository

    @Autowired
    TaskService taskService

    User findById(String id) {
        Optional<User> user = userRepository.findById(id)
        return user.present ? user.get() : null
    }

    User findByTelegramId(String id) {
        return userRepository.findByTelegramChatId(id)
    }

    User findByToken(String token) {
        String userId = tokenService.getUserId(token)
        return findById(userId)
    }

    User createOrUpdateFromGoogle(OAuth2User principal) {
        def attributes = principal.attributes
        def email = attributes.email as String
        def firstName = attributes['given_name'] as String
        def lastName = attributes['family_name'] as String
        def googleUserId = attributes['sub'] as String
        def picture = attributes['picture'] as String

        def user = userRepository.findByEmail(email)
        if (!user) {
            user = new User(
                    email: email,
                    firstName: firstName,
                    lastName: lastName,
                    googleUserId: googleUserId,
                    picture: picture,
                    login: email,
                    isActive: true,
            )
            userRepository.save(user)
        }
        return user
    }

    User createOrUpdateFromFacebook(OAuth2User principal) {
        def attributes = principal.attributes
        def email = attributes.email as String
        def firstName = attributes['given_name'] as String
        def lastName = attributes['last_name'] as String
        def facebookUserId = attributes['id'] as String
        //def picture = attributes['picture'] as String

        def user = userRepository.findByEmail(email)
        if (!user) {
            user = new User(
                    email: email,
                    firstName: firstName,
                    lastName: lastName,
                    facebookUserId: facebookUserId,
                    //picture: picture,
                    login: email,
                    isActive: true,
            )
            userRepository.save(user)
        }
        return user
    }

    Number getMaxAvailableWorkers() {
        // todo : get from database by user
        return 2
    }

    Boolean hasFreeWorker(String userId) {
        def statuses = [PipelineStatuses.RUNNING, PipelineStatuses.QUEUE]
        List<scraper.model.Task> tasks = taskService.findByStatusInAndUserId(statuses, userId)
        if (tasks.size() >= maxAvailableWorkers) {
            // logger.debug("not has free worker for user: ${userId}")
            return false
        }
        return true
    }

    void save(User user) {
        userRepository.save(user)
    }
}
