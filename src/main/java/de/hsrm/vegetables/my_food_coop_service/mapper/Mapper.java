package de.hsrm.vegetables.my_food_coop_service.mapper;

import de.hsrm.vegetables.my_food_coop_service.domain.dto.UserDto;
import de.hsrm.vegetables.my_food_coop_service.model.BalanceResponse;

public class Mapper {

    public static BalanceResponse userDtoToBalanceResponse(UserDto userDto) {
        BalanceResponse result = new BalanceResponse();

        result.setBalance(userDto.getBalance());
        result.setName(userDto.getUsername());

        return result;
    }

}
