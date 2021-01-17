package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.AuthenticationApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.RefreshRequest;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.TokenResponse;
import de.hsrm.vegetables.service.domain.dto.UserDto;
import de.hsrm.vegetables.service.security.UserPrincipal;
import de.hsrm.vegetables.service.services.RefreshTokenService;
import de.hsrm.vegetables.service.services.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class AuthenticationController implements AuthenticationApi {

    @NonNull
    private final RefreshTokenService refreshTokenService;

    @NonNull
    private final UserService userService;

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
