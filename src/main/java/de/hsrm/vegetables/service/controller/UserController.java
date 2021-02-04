package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.UserApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.*;
import de.hsrm.vegetables.service.domain.dto.UserDto;
import de.hsrm.vegetables.service.mapper.UserMapper;
import de.hsrm.vegetables.service.services.BalanceService;
import de.hsrm.vegetables.service.services.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v2")
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
    @PreAuthorize("hasAnyRole('ADMIN','TREASURER')")
    public ResponseEntity<UserListResponse> getUserList(DeleteFilter deleted) {
        List<UserResponse> users = UserMapper.listUserDtoToListUserResponse(userService.getAll(deleted));
        UserListResponse response = new UserListResponse();
        response.setUsers(users);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasRole('MEMBER') and (#userId == authentication.principal.id or hasRole('TREASURER') or hasRole('ADMIN'))")
    public ResponseEntity<UserResponse> userIdGet(String userId) {
        UserResponse response = UserMapper.userDtoToUserResponse(userService.getUserById(userId));
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<Void> userIdDelete(String userId) {
        userService.softDeleteUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    @PreAuthorize("hasRole('MEMBER') and ((#userId == authentication.principal.id and #userPatchRequest.memberId == null) or hasRole('ADMIN'))")
    public ResponseEntity<UserResponse> userPatch(String userId, UserPatchRequest userPatchRequest) {
        UserDto updatedUser = userService.update(
                userId,
                userPatchRequest.getMemberId(),
                userPatchRequest.getEmail(),
                userPatchRequest.getPassword()
        );

        UserResponse response = UserMapper.userDtoToUserResponse(updatedUser);
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
