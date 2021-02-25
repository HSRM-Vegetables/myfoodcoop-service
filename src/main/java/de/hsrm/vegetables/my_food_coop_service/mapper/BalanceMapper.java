package de.hsrm.vegetables.my_food_coop_service.mapper;

import de.hsrm.vegetables.my_food_coop_service.domain.dto.BalanceHistoryItemDto;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.PurchaseDto;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.UserDto;
import de.hsrm.vegetables.my_food_coop_service.model.BalanceHistoryItem;
import de.hsrm.vegetables.my_food_coop_service.model.BalanceResponse;
import de.hsrm.vegetables.my_food_coop_service.model.PurchaseHistoryItem;

public class BalanceMapper {

    private BalanceMapper() {
        // hide implicit public constructor
    }

    public static BalanceResponse userDtoToBalanceResponse(UserDto userDto) {
        BalanceResponse result = new BalanceResponse();

        result.setBalance(userDto.getBalance());
        result.setName(userDto.getUsername());

        return result;
    }

    public static BalanceHistoryItem balanceHistoryItemDtoToBalanceHistoryItem(BalanceHistoryItemDto balanceHistoryItemDto) {
        BalanceHistoryItem balanceHistoryItem = new BalanceHistoryItem();

        PurchaseDto purchaseDto = balanceHistoryItemDto.getPurchase();
        PurchaseHistoryItem purchaseHistoryItem =
                purchaseDto == null ? null : PurchaseMapper.purchaseDtoToPurchaseHistoryItem(purchaseDto);

        balanceHistoryItem.setId(balanceHistoryItemDto.getId());
        balanceHistoryItem.setPurchase(purchaseHistoryItem);
        balanceHistoryItem.setCreatedOn(balanceHistoryItemDto.getCreatedOn());
        balanceHistoryItem.setBalanceChangeType(balanceHistoryItemDto.getBalanceChangeType());
        balanceHistoryItem.setAmount(balanceHistoryItemDto.getAmount());

        return balanceHistoryItem;
    }
}
