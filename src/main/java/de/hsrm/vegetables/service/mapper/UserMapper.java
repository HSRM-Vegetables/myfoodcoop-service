package de.hsrm.vegetables.service.mapper;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.UserResponse;
import de.hsrm.vegetables.service.domain.dto.UserDto;

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

        return userResponse;
    }


}
