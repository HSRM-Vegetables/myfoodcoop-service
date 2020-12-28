package de.hsrm.vegetables.service.mapper;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceResponse;
import de.hsrm.vegetables.service.domain.dto.BalanceDto;

public class Mapper {

    public static BalanceResponse balanceDtoToBalanceResponse(BalanceDto balance) {
        BalanceResponse result = new BalanceResponse();

        result.setBalance(balance.getAmount());
        result.setName(balance.getName());

        return result;
    }

}
