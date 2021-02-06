package de.hsrm.vegetables.service.mapper;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceResponse;
import de.hsrm.vegetables.service.domain.dto.UserDto;

public class Mapper {

    public static BalanceResponse userDtoToBalanceResponse(UserDto userDto) {
        BalanceResponse result = new BalanceResponse();

        result.setBalance(userDto.getBalance());
        result.setName(userDto.getUsername());

        return result;
    }

}
