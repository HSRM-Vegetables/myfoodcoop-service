package de.hsrm.vegetables.service.services;

import de.hsrm.vegetables.service.domain.dto.RefreshTokenDto;
import de.hsrm.vegetables.service.domain.dto.UserDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.UnauthorizedError;
import de.hsrm.vegetables.service.exception.errors.security.ExpiredTokenException;
import de.hsrm.vegetables.service.repositories.RefreshTokenRepository;
import de.hsrm.vegetables.service.security.JwtUtil;
import de.hsrm.vegetables.service.security.UserPrincipal;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class RefreshTokenService {

    @NonNull
    private final RefreshTokenRepository refreshTokenRepository;

    @NonNull
    private final JwtUtil jwtUtil;

    @Value("${vegetables.jwt.secret}")
    private String jwtSecret;

    @Value("${vegetables.jwt.refreshLifetime}")
    private Integer jwtRefreshLifetime;

    public String generateRefreshToken(UserDto user) {
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto();

        // Generate and set refreshToken
        String refreshToken = JwtUtil.generateRefreshToken(user.getUsername(), user.getId(), jwtRefreshLifetime, jwtSecret);
        refreshTokenDto.setRefreshToken(refreshToken);

        // Associate user
        refreshTokenDto.setUser(user);

        return refreshTokenRepository.save(refreshTokenDto)
                .getRefreshToken();
    }

    /**
     * Checks validity of the current refresh token, revokes it and returns a new one
     * If anything fishy is going on with this refreshToken, it is revoked
     *
     * @param user                The user who's supposed to be associated to this token
     * @param currentRefreshToken The refresh token to check
     * @return A new refreshToken for the user
     */
    @Transactional
    public String getNewRefreshTokenAndRevokeCurrentOne(UserDto userDto, String currentRefreshToken) {
        revokeRefreshToken(userDto, currentRefreshToken);

        // Return a new refresh token
        return generateRefreshToken(userDto);
    }

    /**
     * Revokes all refreshTokens associated to this user
     *
     * @param user The user to revoke the refreshTokens for
     */
    @Transactional
    public void revokeAllRefreshTokens(UserDto user) {
        refreshTokenRepository.deleteAllByUser(user);
    }

    /**
     * Revokes a refresh token
     * The token needs to be valid and the user data needs to match the data within refreshToken
     * refreshToken will be revoked if anything fishy is going on
     *
     * @param user         The user to check the token against
     * @param refreshToken The refreshToken to revoke
     */
    @Transactional
    public void revokeRefreshToken(UserDto user, String refreshToken) {
        UserPrincipal tokenUserData = getUserDataFromRefreshToken(refreshToken);

        checkTokenDataAndUserMatch(user, tokenUserData, refreshToken);

        // Check if token was revoked already
        if (!refreshTokenRepository.existsByRefreshToken(refreshToken)) {
            throw new UnauthorizedError("Refresh token was revoked!", ErrorCode.REFRESH_TOKEN_REVOKED);
        }

        // Revoke this refresh token
        refreshTokenRepository.deleteByRefreshToken(refreshToken);
    }

    public String getUseridFromTokenUnchecked(String token) {
        UserPrincipal tokenUserData = jwtUtil.parseRefreshToken(token, jwtSecret);
        return tokenUserData.getId();
    }

    /**
     * Gets data stored in refreshToken.
     * If token is expired, it will be deleted from the database
     *
     * @param token The token to get the data from
     * @return The userData contained within the token
     */
    private UserPrincipal getUserDataFromRefreshToken(String token) {
        UserPrincipal tokenUserData;
        try {
            tokenUserData = jwtUtil.parseRefreshToken(token, jwtSecret);
        } catch (ExpiredTokenException e) {
            // Remove token from Database
            refreshTokenRepository.deleteByRefreshToken(token);
            // Throw this error again, so the user gets his neat error message
            throw e;
        }
        return tokenUserData;
    }

    /**
     * Checks that the data from within the token and the user data passed to this function match
     * If they don't the token is revoked and an error is thrown
     *
     * @param userDto       The user to check the token against
     * @param userPrincipal The data from within the token
     * @param refreshToken  The token where userPrincipal data was parsed from
     */
    private void checkTokenDataAndUserMatch(UserDto userDto, UserPrincipal userPrincipal, String refreshToken) {
        // Check that the refreshToken data matches the user passed to this function
        if (!userPrincipal.getId()
                .equals(userDto.getId())) {
            // Revoke this refreshToken as it might have been stolen
            refreshTokenRepository.deleteByRefreshToken(refreshToken);
            throw new UnauthorizedError("Refresh token does not match authenticated user", ErrorCode.REFRESH_TOKEN_NOT_MATCHES_AUTHENTICATED_USER);
        }
    }
}
