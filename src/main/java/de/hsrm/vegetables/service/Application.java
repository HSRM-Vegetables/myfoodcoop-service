package de.hsrm.vegetables.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@ComponentScan(basePackages = "de.hsrm")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Configuration
    public static class Config {

        @Bean
        public PasswordEncoder encoder() {
            return new BCryptPasswordEncoder();
        }

    }

    @Configuration
    public static class SecurityConfiguration extends WebSecurityConfigurerAdapter {

        @Override
        public void configure(final WebSecurity webSecurity) {
            webSecurity.ignoring()
                    .antMatchers("/**");
        }
    }
}
