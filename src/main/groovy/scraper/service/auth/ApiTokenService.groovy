package scraper.service.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.CompressionCodecs
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.constants.ApiLevel

@Service
class ApiTokenService {

    public static final String KEY = "apitoken"

    @Autowired
    JwtTokenService jwtTokenService

    /**
     * Gets token sting.
     * @param userId
     * @return Token string if password is valid.
     * @throws Exception
     */
    String createToken(String userId, ApiLevel apiLevel) throws Exception {
        String compactJws =  Jwts.builder()
                .setId(userId)
                .setHeaderParam('apiLevel', apiLevel)
                .signWith(SignatureAlgorithm.HS256, KEY)
                .compact()
        return compactJws
    }

    /**
     * Checks token is a valid.
     * @param token
     * @return
     */
    boolean checkToken(String token) {
        Claims claims = jwtTokenService.parseToken(token)
        if (!claims) {
            return false
        }
        return true
    }
}
