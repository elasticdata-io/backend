package scraper.service.config;

import scraper.service.config.oauth2.Oauth2UrlAuthenticationSuccessHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableAuthorizationServer
public class AuthConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
            .oauth2Login()
				.successHandler(myAuthenticationSuccessHandler())
            .and()
            .authorizeRequests()
            .anyRequest().permitAll()
			.and()
			.csrf().disable();
	}

    private AuthenticationSuccessHandler myAuthenticationSuccessHandler() {
		return new Oauth2UrlAuthenticationSuccessHandler();
	}
}
