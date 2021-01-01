package scraper.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.model.User
import scraper.model.UserToken

@Service
class UserTokenService {

    @Autowired
    scraper.repository.UserTokenRepository userTokenRepository


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
