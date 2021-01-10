package scraper.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import scraper.constants.ApiLevel
import scraper.constants.PipelineStatuses
import scraper.model.Task
import scraper.model.User
import scraper.repository.UserRepository
import scraper.service.auth.ApiTokenService
import scraper.service.auth.JwtTokenService

@Service
class UserService {

    @Autowired
    JwtTokenService jwtTokenService

    @Autowired
    ApiTokenService apiTokenService

    @Autowired
    UserRepository userRepository

    @Autowired
    TaskService taskService

    @Autowired
    TariffPlanService tariffPlanService

    User findById(String id) {
        Optional<User> user = userRepository.findById(id)
        return user.present ? user.get() : null
    }

    User findByTelegramId(String id) {
        return userRepository.findByTelegramChatId(id)
    }

    User findByToken(String token) {
        String userId = jwtTokenService.getUserId(token)
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
        if (!user.apiToken) {
            user.apiToken = apiTokenService.createToken(email, ApiLevel.THIRD_PARTY_APP)
            userRepository.save(user)
        }
        return user
    }

    User createOrUpdateFromFacebook(OAuth2User principal) {
        def attributes = principal.attributes
        def facebookUserId = attributes['id'] as String
        def email = attributes.email as String
        def firstName = attributes['given_name'] as String
        def lastName = attributes['last_name'] as String
        //def picture = attributes['picture'] as String
        def user = userRepository.findByEmail(email)
        if (!user) {
            user = new User(
                    email: email ?: facebookUserId,
                    firstName: firstName,
                    lastName: lastName,
                    facebookUserId: facebookUserId,
                    //picture: picture,
                    login: email,
                    isActive: true,
            )
            userRepository.save(user)
        }
        if (!user.apiToken) {
            user.apiToken = apiTokenService.createToken(user.id, ApiLevel.THIRD_PARTY_APP)
            userRepository.save(user)
        }
        return user
    }

    Number getMaxAvailableWorkers(String userId) {
        def tariffPlan = tariffPlanService.getTariffPlanByUserId(userId)
        def privateWorkers = tariffPlan.configuration.privateWorkers as double
        def sharedWorkers = tariffPlan.configuration.sharedWorkers as double
        return Math.max(privateWorkers, sharedWorkers)
    }

    Boolean hasFreeWorker(String userId) {
        def statuses = [PipelineStatuses.RUNNING, PipelineStatuses.QUEUE]
        List<Task> tasks = taskService.findByStatusInAndUserId(statuses, userId)
        if (tasks.size() >= getMaxAvailableWorkers(userId)) {
            // logger.debug("not has free worker for user: ${userId}")
            return false
        }
        return true
    }

    void save(User user) {
        userRepository.save(user)
    }
}
