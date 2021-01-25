package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.UsersApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.*;
import de.hsrm.vegetables.service.domain.dto.UserDto;
import de.hsrm.vegetables.service.mapper.StockMapper;
import de.hsrm.vegetables.service.mapper.UserMapper;
import de.hsrm.vegetables.service.services.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v2")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class UsersController implements UsersApi {

    @NonNull
    private final UserService userService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserListResponse> usersGet(DeleteFilter deleted) {
        List<UserResponse> users = UserMapper.listUserDtoToListUserResponse(userService.getAll(deleted));
        UserListResponse response = new UserListResponse();
        response.setUsers(users);
        return ResponseEntity.ok(response);
    }

}
