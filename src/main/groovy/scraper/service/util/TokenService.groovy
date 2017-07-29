package scraper.service.util

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
import scraper.service.repository.UserRepository

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse
import javax.xml.bind.DatatypeConverter

@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class)

    public static final String KEY = "sad234234asd12312k_!sdasd"

    public final static String EMAIL = "email"
    public final static String LOGIN = "login"
    public final static String FIRST_NAME = "firstName"
    public final static String SECOND_NAME = "secondName"

    @Autowired
    private UserRepository userRepository

    /**
     * Gets token sting.
     * @param login
     * @param password
     * @return Token string if password is valid.
     * @throws Exception
     */
    public String makeToken(String login, String password) throws Exception {
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
    public String makeToken(String login) throws Exception {
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
                .claim(EMAIL, user.email)
                .claim(LOGIN, user.login)
                .claim(FIRST_NAME, user.firstName)
                .claim(SECOND_NAME, user.secondName)
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
    public Claims parseToken(String jwtToken) {
        Claims claims
        try {
            claims = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(KEY))
                    .parseClaimsJws(jwtToken).getBody()
        } catch (Exception e) {
            return null
        }
        return claims
    }

    /**
     * Registers token to cookie store.
     * @param token
     * @param response
     */
    public void registerTokenToCookie(String token, HttpServletResponse response) {
        Cookie cookie = new Cookie("token", token)
        cookie.setPath("/")
        response.addCookie(cookie)
    }
}
