package scraper.util

import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import java.util.regex.Matcher
import java.util.regex.Pattern

@Service
class EmailValidator {

    private Pattern pattern
    private Matcher matcher

    private static final String EMAIL_PATTERN =
            '^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$';

    @PostConstruct
    void init() {
        pattern = Pattern.compile(EMAIL_PATTERN)
    }

    /**
     * Validate hex with regular expression
     *
     * @param hex for validation
     * @return true valid hex, false invalid hex
     */
    boolean validate(final String hex) {
        matcher = pattern.matcher(hex)
        return matcher.matches()
    }
}
