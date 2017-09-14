package scraper.service.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import scraper.service.config.request.filter.TokenFilter

@Configuration
@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    TokenFilter tokenFilter

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterAfter(tokenFilter, BasicAuthenticationFilter.class)
        http.csrf().disable()
            .authorizeRequests()
            .anyRequest()
            .permitAll()
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
    }
}
