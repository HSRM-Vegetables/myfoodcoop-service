package de.hsrm.vegetables.service.security;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${vegetables.jwt.secret}")
    private String jwtSecret;

    @NonNull
    private final AccessViolationExceptionHandler accessViolationExceptionHandler;

    @NonNull
    private final SecurityExceptionHandler securityExceptionHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // No need for CSRF protection
                .csrf()
                .disable()
                // Pass exceptions to the Global Exception Handler
                .addFilterAfter(accessViolationExceptionHandler, ExceptionTranslationFilter.class)
                // We need two different exception handlers that basically do the same...
                .addFilterAfter(securityExceptionHandler, LogoutFilter.class)
                // We're a stateless API
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // V1 does not require authentication
                .antMatchers("/v1/**")
                .permitAll()
                // login and register do not require authentication
                .mvcMatchers("/v2/user/login", "/v2/user/register")
                .permitAll()
                // OPTIONS requests do not need authorization
                .antMatchers(HttpMethod.OPTIONS)
                .permitAll()
                // Health actuator does not need authentication
                .mvcMatchers("/actuator/health")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtSecret),
                        UsernamePasswordAuthenticationFilter.class)
        ;
    }

}