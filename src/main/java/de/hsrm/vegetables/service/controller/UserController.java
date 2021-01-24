package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.UserApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.RegisterRequest;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.Role;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.UserResponse;
import de.hsrm.vegetables.service.domain.dto.UserDto;
import de.hsrm.vegetables.service.mapper.UserMapper;
import de.hsrm.vegetables.service.security.UserPrincipal;
import de.hsrm.vegetables.service.services.BalanceService;
import de.hsrm.vegetables.service.services.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class UserController implements UserApi {

    @NonNull
    private final UserService userService;

    @NonNull
    private final BalanceService balanceService;

    @Override
    public ResponseEntity<UserResponse> register(RegisterRequest registerRequest) {

        UserDto newUser = userService.register(registerRequest.getUsername(), registerRequest.getEmail(), registerRequest.getMemberId(), registerRequest.getPassword());

        balanceService.upsert(newUser.getUsername(), 0f);

        UserResponse response = UserMapper.userDtoToUserResponse(newUser);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> userDelete() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        userService.softDeleteUser(userPrincipal.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<UserResponse> userGet() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        UserResponse response = UserMapper.userDtoToUserResponse(userService.getUserById(userPrincipal.getId()));

        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> userAddRole(String userId, String role) {
        UserResponse response = UserMapper.userDtoToUserResponse(userService.addRole(userId, Role.valueOf(role)));
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> userDeleteRoles(String userId, String role) {
        UserResponse response = UserMapper.userDtoToUserResponse(userService.deleteRole(userId, Role.valueOf(role)));
        return ResponseEntity.ok(response);
    }
}
