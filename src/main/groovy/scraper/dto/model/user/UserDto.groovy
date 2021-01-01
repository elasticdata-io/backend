package scraper.dto.model.user

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class UserDto {
    String id
    String login
    String email
    String firstName
    String lastName
    String picture
    Boolean isAdmin
    Boolean hasTelegram
}