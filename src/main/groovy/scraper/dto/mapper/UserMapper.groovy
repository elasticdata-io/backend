package scraper.dto.mapper

import org.springframework.stereotype.Component
import scraper.dto.model.user.UserDto
import scraper.model.User

@Component
class UserMapper {

    static UserDto toUserDto(User user) {
        return new UserDto(
            id: user.id,
            email: user.email,
            login: user.login,
            lastName: user.lastName,
            firstName: user.firstName,
            picture: user.picture,
            isAdmin: user.isAdmin,
            hasTelegram: user.telegramChatId,
            apiToken: user.apiToken
        )
    }

}
