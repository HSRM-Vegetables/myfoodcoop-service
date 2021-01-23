package de.hsrm.vegetables.service;

import de.hsrm.vegetables.service.interceptors.RequestLoggerInterceptor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
    @RequiredArgsConstructor(onConstructor = @__({@Autowired}))
    public static class InterceptorConfiguration implements WebMvcConfigurer {

        @NonNull
        private final RequestLoggerInterceptor requestLoggerInterceptor;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(requestLoggerInterceptor)
                    .addPathPatterns("/v2/**");
        }
    }
}
