package scraper.service.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.CompressionCodecs
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import scraper.service.model.User
import scraper.service.model.UserToken
import scraper.service.repository.UserRepository
import scraper.service.repository.UserTokenRepository

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.xml.bind.DatatypeConverter

@Service
class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class)

    public static final String KEY = "sad234234asd12312k_!sdasd"

    @Autowired
    private UserRepository userRepository

    @Autowired
    private UserTokenRepository userTokenRepository

    /**
     * Gets token sting.
     * @param login
     * @param password
     * @return Token string if password is valid.
     * @throws Exception
     */
    String makeToken(String login, String password) throws Exception {
        User user = userRepository.findByLogin(login)
        if (user == null || user.isActive == null || !user.isActive) {
            return null
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder()
        boolean isValidPassword = passwordEncoder.matches(password + user.salt, user.password)
        if (!isValidPassword) {
            return null
        }
        return makeToken(login)
    }

    /**
     * Gets token sting.
     * @param login
     * @return Token string if password is valid.
     * @throws Exception
     */
    String makeToken(String login) throws Exception {
        User user = userRepository.findByLogin(login)
        if (user == null || user.isActive == null || !user.isActive) {
            return null
        }
        Date currentDate = new Date()
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(currentDate)
        calendar.add(Calendar.HOUR, 72)
        String compactJws =  Jwts.builder()
                .setId(user.id)
                .setSubject(user.login)
                .compressWith(CompressionCodecs.DEFLATE)
                .signWith(SignatureAlgorithm.HS512, KEY)
                .setExpiration(calendar.getTime())
                .compact()
        logger.info("make new token {}", compactJws)
        return compactJws
    }

    /**
     * Parsing jwt token and return data.
     * @param jwtToken
     * @return User data from jwt token.
     */
    Claims parseToken(String jwtToken) {
        if (!jwtToken) {
            return null
        }
        Claims claims
        try {
            claims = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(KEY))
                    .parseClaimsJws(jwtToken).getBody()
        } catch (Exception e) {
            logger.error('token service error', e)
            return null
        }
        return claims
    }

    /**
     * Gets user id by jwt token
     * @param jwtToken
     * @return
     */
    String getUserId(String jwtToken) {
        Claims claims = parseToken(jwtToken)
        if (!claims) {
            return null
        }
        return claims.getId()
    }

    /**
     * @deprecated
     * Registers token to cookie store.
     * @param token
     * @param response
     */
    void registerTokenToCookie(String token, HttpServletResponse response) {
        Cookie cookie = new Cookie("token", token)
        cookie.setPath("/")
        response.addCookie(cookie)
    }

    /**
     * TODO check single of the responsibility
     * Saves token to db.
     * @param token
     */
    void saveUserToken(String token, HttpServletRequest request) {
        Claims claims = parseToken(token)
        String login = claims.getSubject()
        User user = userRepository.findByLogin(login)
        UserToken userToken = new UserToken(
                token: token,
                user: user,
                ip: request.getHeader('X-FORWARDED-FOR') ?: request.getRemoteAddr(),
                userAgent: request.getHeader('User-Agent'),
                createdOn: new Date())
        userTokenRepository.save(userToken)
    }

    /**
     * Checks token is a valid.
     * @param token
     * @return
     */
    boolean checkToken(String token) {
        Claims claims = parseToken(token)
        if (!claims) {
            return false
        }
        boolean isExpired = claims.getExpiration() <= new Date()
        if (isExpired) {
            return false
        }
        UserToken userToken = userTokenRepository.findByToken(token)
        return userToken != null
    }
}
