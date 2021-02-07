package de.hsrm.vegetables.service.mapper;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceHistoryItem;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceResponse;
import de.hsrm.vegetables.service.domain.dto.BalanceHistoryItemDto;
import de.hsrm.vegetables.service.domain.dto.UserDto;

public class BalanceMapper {

    public static BalanceResponse userDtoToBalanceResponse(UserDto userDto) {
        BalanceResponse result = new BalanceResponse();

        result.setBalance(userDto.getBalance());
        result.setName(userDto.getUsername());

        return result;
    }

    public static BalanceHistoryItem balanceHistoryItemDtoToBalanceHistoryItem(BalanceHistoryItemDto balanceHistoryItemDto) {
        BalanceHistoryItem balanceHistoryItem = new BalanceHistoryItem();

        balanceHistoryItem.setId(balanceHistoryItemDto.getId());
        balanceHistoryItem.setCreatedOn(balanceHistoryItemDto.getCreatedOn());
        balanceHistoryItem.setBalanceChangeType(balanceHistoryItemDto.getBalanceChangeType());
        balanceHistoryItem.setAmount(balanceHistoryItemDto.getAmount());

        return balanceHistoryItem;
    }
}
