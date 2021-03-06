package de.hsrm.vegetables.my_food_coop_service.controller;

import de.hsrm.vegetables.my_food_coop_service.api.AuthenticationApi;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.UserDto;
import de.hsrm.vegetables.my_food_coop_service.exception.ErrorCode;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.http.NotFoundError;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.http.UnauthorizedError;
import de.hsrm.vegetables.my_food_coop_service.model.LoginRequest;
import de.hsrm.vegetables.my_food_coop_service.model.RefreshRequest;
import de.hsrm.vegetables.my_food_coop_service.model.Role;
import de.hsrm.vegetables.my_food_coop_service.model.TokenResponse;
import de.hsrm.vegetables.my_food_coop_service.security.UserPrincipal;
import de.hsrm.vegetables.my_food_coop_service.services.RefreshTokenService;
import de.hsrm.vegetables.my_food_coop_service.services.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class AuthenticationController implements AuthenticationApi {

    @NonNull
    private final RefreshTokenService refreshTokenService;

    @NonNull
    private final UserService userService;

    @Override
    public ResponseEntity<TokenResponse> login(LoginRequest loginRequest) {
        TokenResponse response = new TokenResponse();
        UserDto user;

        try {
            user = userService.getUserByUsername(loginRequest.getUsername());

        } catch (NotFoundError e) {
            // Mask not found error
            throw new UnauthorizedError("Username or password incorrect", ErrorCode.USERNAME_OR_PASSWORD_WRONG);
        }

        // ony user with role Member can access
        List<Role> roles = user.getRoles();
        if (!roles.contains(Role.MEMBER)) {
            throw new UnauthorizedError("User does not belong to role 'Member'", ErrorCode.USER_NOT_ACTIVATED);
        }

        String token = userService.generateToken(user, loginRequest.getPassword());
        String refreshToken = refreshTokenService.generateRefreshToken(user);

        response.setToken(token);
        response.setRefreshToken(refreshToken);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<TokenResponse> refresh(RefreshRequest refreshRequest) {
        String userId = refreshTokenService.getUseridFromTokenUnchecked(refreshRequest.getRefreshToken());
        UserDto userDto = userService.getUserById(userId);

        String refreshToken = refreshTokenService.getNewRefreshTokenAndRevokeCurrentOne(userDto, refreshRequest.getRefreshToken());
        String token = userService.generateToken(userDto);

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setToken(token);
        tokenResponse.setRefreshToken(refreshToken);

        return ResponseEntity.ok(tokenResponse);
    }

    @Override
    public ResponseEntity<Void> revoke(RefreshRequest refreshRequest) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        UserDto userDto = userService.getUserById(userPrincipal.getId());

        refreshTokenService.revokeRefreshToken(userDto, refreshRequest.getRefreshToken());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> revokeAll() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        UserDto userDto = userService.getUserById(userPrincipal.getId());

        refreshTokenService.revokeAllRefreshTokens(userDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
