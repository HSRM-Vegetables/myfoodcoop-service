package de.hsrm.vegetables.service.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.UnauthorizedError;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    public static String generateToken(String username, String userId, Integer jwtLifetime, String jwtSecret) {

        return JWT.create()
                .withSubject(username)
                .withClaim("id", userId)
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtLifetime))
                .withIssuedAt(new Date())
                .sign(Algorithm.HMAC512(jwtSecret.getBytes(StandardCharsets.UTF_8)));
    }

    public static UserPrincipal parseToken(String token, String jwtSecret) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .verify(token);

        Date expiresAt = decodedJWT.getExpiresAt();

        if (expiresAt.before(new Date())) {
            throw new UnauthorizedError("Token expired", ErrorCode.TOKEN_EXPIRED);
        }

        UserPrincipal userPrincipal = new UserPrincipal();

        userPrincipal.setUsername(decodedJWT.getSubject());

        userPrincipal.setId(decodedJWT.getClaim("id")
                .asString());

        return userPrincipal;
    }

}
