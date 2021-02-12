package de.hsrm.vegetables.my_food_coop_service.mapper;

import de.hsrm.vegetables.my_food_coop_service.domain.dto.UserDto;
import de.hsrm.vegetables.my_food_coop_service.model.UserResponse;

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
        userResponse.setIsDeleted(userDto.isDeleted());

        return userResponse;
    }

    public static List<UserResponse> listUserDtoToListUserResponse(List<UserDto> userDtos) {
        return userDtos
                .stream()
                .map(UserMapper::userDtoToUserResponse)
                .collect(Collectors.toList());
    }

}
