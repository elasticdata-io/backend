package scraper.service.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import scraper.service.auth.TokenService
import scraper.service.constants.PipelineStatuses
import scraper.service.model.Task
import scraper.service.model.User
import scraper.service.repository.UserRepository

@Service
class UserService {

    private static Logger logger = LoggerFactory.getLogger(UserService.class);

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
        List<Task> tasks = taskService.findByStatusInAndUserId(statuses, userId)
        if (tasks.size() >= maxAvailableWorkers) {
            logger.debug("not has free worker for user: ${userId}")
            return false
        }
        return true
    }
}
