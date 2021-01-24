package de.hsrm.vegetables.service.mapper;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.UserResponse;
import de.hsrm.vegetables.service.domain.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    private UserMapper() {
        // Hide public constructor
    }

    public static UserResponse userDtoToUserResponse(UserDto userDto) {
        UserResponse userResponse = new UserResponse();
        userResponse.setUsername(userDto.getUsername());
        userResponse.setEmail(userDto.getEmail());
        userResponse.setMemberId(userDto.getMemberId());
        userResponse.setId(userDto.getId());
        userResponse.setRoles(userDto.getRoles());

        return userResponse;
    }

    public static List<UserResponse> listUserDtoToListUserResponse(List<UserDto> userDtos) {
        return userDtos
                .stream()
                .map(UserMapper::userDtoToUserResponse)
                .collect(Collectors.toList());
    }

}
