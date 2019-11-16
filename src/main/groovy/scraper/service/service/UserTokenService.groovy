package scraper.service.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.service.model.User
import scraper.service.model.UserToken
import scraper.service.repository.UserTokenRepository

@Service
class UserTokenService {

    @Autowired
    UserTokenRepository userTokenRepository


    UserToken findLastToken(User user) {
        List<UserToken> tokens = userTokenRepository.findByUserOrderByCreatedOnDesc(user)
        if (!tokens) {
            return
        }
        if(tokens.empty) {
            return
        }
        return tokens.first()
    }
}
