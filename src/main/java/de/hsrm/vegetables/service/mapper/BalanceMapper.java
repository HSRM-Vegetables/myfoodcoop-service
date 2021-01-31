package de.hsrm.vegetables.service.mapper;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceHistoryItem;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceResponse;
import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.domain.dto.BalanceHistoryItemDto;

public class BalanceMapper {

    public static BalanceResponse balanceDtoToBalanceResponse(BalanceDto balance) {
        BalanceResponse result = new BalanceResponse();

        result.setBalance(balance.getAmount());
        result.setName(balance.getName());

        return result;
    }

    public static BalanceHistoryItem balanceHistoryItemDtoToBalanceHistoryItem(BalanceHistoryItemDto balanceHistoryItemDto) {
        BalanceHistoryItem balanceHistoryItem = new BalanceHistoryItem();

        balanceHistoryItem.setId(balanceHistoryItemDto.getId());
        balanceHistoryItem.setCreatedOn(balanceHistoryItemDto.getCreatedOn());
        balanceHistoryItem.setAmount(balanceHistoryItemDto.getAmount());

        return balanceHistoryItem;
    }
}
