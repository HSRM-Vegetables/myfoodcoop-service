package de.hsrm.vegetables.my_food_coop_service.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.hsrm.vegetables.my_food_coop_service.exception.ErrorCode;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.http.NotFoundError;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.http.UnauthorizedError;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.security.ExpiredTokenException;
import de.hsrm.vegetables.my_food_coop_service.model.Role;
import de.hsrm.vegetables.my_food_coop_service.services.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class JwtUtil {

    @NonNull
    private final UserService userService;

    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private static final String AUTHENTICATION_TOKEN_TYPE = "authentication";

    private static final String JWT_ROLES_KEY = "roles";

    /**
     * Generates a token string for authentication
     *
     * @param username    Username of user this token is for
     * @param userId      Userid of user this token is for
     * @param jwtLifetime ms. How long this token is valid for
     * @param jwtSecret   Secret to sign token with
     * @return The token string
     */
    public static String generateToken(String username, String userId, List<Role> roles, Integer jwtLifetime, String jwtSecret) {

        return JWT.create()
                .withSubject(username)
                .withClaim("id", userId)
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtLifetime))
                .withIssuedAt(new Date())
                .withClaim("type", AUTHENTICATION_TOKEN_TYPE)
                .withClaim(JWT_ROLES_KEY, roles.stream()
                        .map(Role::toString)
                        .collect(Collectors.toList()))
                .sign(Algorithm.HMAC512(jwtSecret.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Generates a raw refresh token string
     * Important: This token needs to be managed by the RefreshTokenService! Do not use standalone!
     *
     * @param username    Username of user this token is for
     * @param userId      Userid of user this token is for
     * @param jwtLifetime ms. How long this token is valid for
     * @param jwtSecret   Secret to sign token with
     * @return The token string
     */
    public static String generateRefreshToken(String username, String userId, Integer jwtLifetime, String jwtSecret) {
        return JWT.create()
                .withSubject(username)
                .withClaim("id", userId)
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtLifetime))
                .withIssuedAt(new Date())
                .withClaim("type", REFRESH_TOKEN_TYPE)
                .sign(Algorithm.HMAC512(jwtSecret.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Parses a authentication token and returns user data within it.
     * Throws error if token is expired, signature is incorrect or token is of incorrect type
     *
     * @param token     The token to parse
     * @param jwtSecret The secret to check the tokens signature agains
     * @return The user data contained within the token
     */
    public UserPrincipal parseAuthenticationToken(String token, String jwtSecret) {
        return parseAuthenticationToken(token, jwtSecret, AUTHENTICATION_TOKEN_TYPE);
    }

    /**
     * Parses a refresh token and returns user data within it.
     * Throws error if token is expired, signature is incorrect or token is of incorrect type
     *
     * @param token     The token to parse
     * @param jwtSecret The secret to check the tokens signature agains
     * @return The user data contained within the token
     */
    public UserPrincipal parseRefreshToken(String token, String jwtSecret) {
        return parseAuthenticationToken(token, jwtSecret, REFRESH_TOKEN_TYPE);
    }

    private UserPrincipal parseAuthenticationToken(String token, String jwtSecret, String tokenType) {
        DecodedJWT decodedJWT = decodeToken(token, jwtSecret);

        // Check expiration, correct type, etc.
        checkJwtToken(decodedJWT, tokenType);

        // Extract user info from token
        UserPrincipal userPrincipal = new UserPrincipal();
        userPrincipal.setUsername(decodedJWT.getSubject());
        userPrincipal.setId(decodedJWT.getClaim("id")
                .asString());
        userPrincipal.setRoles(decodedJWT.getClaim(JWT_ROLES_KEY)
                .asList(Role.class));

        // Check that user associated to this JWT is not deleted and exists
        try {
            if (userService.isDeleted(userPrincipal.getId())) {
                throw new UnauthorizedError("Token invalid", ErrorCode.INVALID_JWT);
            }
        } catch (NotFoundError error) {
            throw new UnauthorizedError("Token invalid", ErrorCode.INVALID_JWT);
        }

        return userPrincipal;
    }

    private DecodedJWT decodeToken(String token, String jwtSecret) {
        return JWT.require(Algorithm.HMAC512(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .verify(token);
    }

    private void checkJwtToken(DecodedJWT decodedJWT, String expectedType) {
        Date expiresAt = decodedJWT.getExpiresAt();

        if (expiresAt.before(new Date())) {
            throw new ExpiredTokenException("Token expired", ErrorCode.TOKEN_EXPIRED);
        }

        String tokenType = decodedJWT.getClaim("type")
                .asString();

        if (!tokenType.equals(expectedType)) {
            throw new UnauthorizedError("A authentication token is required to authenticate, but a different type was provided", ErrorCode.AUTHENTICATION_TOKEN_REQUIRED);
        }
    }

}
