package de.hsrm.vegetables.service.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Adapted from:
// https://dev.to/d_tomov/jwt-bearer-authentication-authorization-with-spring-security-5-in-a-spring-boot-app-2cfe

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final String jwtSecret;

    private final JwtUtil jwtUtil;

    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";

    private static final String HEADER_TOKEN_PREFIX = "Bearer ";

    public JwtAuthenticationFilter(String jwtSecret, JwtUtil jwtUtil) {
        this.jwtSecret = jwtSecret;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws IOException, ServletException {
        String authorizationHeader = httpServletRequest.getHeader(AUTHORIZATION_HEADER_NAME);

        if (authorizationHeaderIsInvalid(authorizationHeader)) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        UsernamePasswordAuthenticationToken token = createToken(authorizationHeader);

        SecurityContextHolder.getContext()
                .setAuthentication(token);
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private boolean authorizationHeaderIsInvalid(String authorizationHeader) {
        return authorizationHeader == null
                || !authorizationHeader.startsWith(HEADER_TOKEN_PREFIX);
    }

    // Retrieves token information from Request header,
    // parses it and returns a token to be used in the spring security context
    private UsernamePasswordAuthenticationToken createToken(String authorizationHeader) {
        // Strip header value prefix to get raw token
        String token = authorizationHeader.replace(HEADER_TOKEN_PREFIX, "");

        // Retrieve Information stored in token
        UserPrincipal userPrincipal = jwtUtil.parseAuthenticationToken(token, jwtSecret);

        List<GrantedAuthority> authorities = new ArrayList<>();

        userPrincipal.getRoles()
                .forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toString())));

        return new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
    }
}