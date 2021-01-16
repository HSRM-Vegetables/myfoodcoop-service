package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.UserApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.RegisterRequest;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.UserResponse;
import de.hsrm.vegetables.service.domain.dto.UserDto;
import de.hsrm.vegetables.service.mapper.UserMapper;
import de.hsrm.vegetables.service.services.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Override
    public ResponseEntity<UserResponse> register(RegisterRequest registerRequest) {

        UserDto newUser = userService.register(registerRequest.getUsername(), registerRequest.getEmail(), registerRequest.getMemberId(), registerRequest.getPassword());
        UserResponse response = UserMapper.userDtoToUserResponse(newUser);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

}
