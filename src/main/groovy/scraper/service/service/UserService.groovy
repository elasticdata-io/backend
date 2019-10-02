package scraper.service.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import scraper.service.auth.TokenService
import scraper.service.model.User
import scraper.service.repository.UserRepository

@Service
class UserService {

    @Autowired
    TokenService tokenService

    @Autowired
    UserRepository userRepository

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
                    login: email
            )
            userRepository.save(user)
        }
        return user
    }
}
