package de.hsrm.vegetables.my_food_coop_service.mapper;

import de.hsrm.vegetables.my_food_coop_service.domain.dto.UserDto;
import de.hsrm.vegetables.my_food_coop_service.model.BalanceOverviewItem;

public class ReportsMapper {

    private ReportsMapper() {
        // hide implicit public constructor
    }

    public static BalanceOverviewItem userDtoToBalanceOverviewItem(UserDto userDto) {
        BalanceOverviewItem item = new BalanceOverviewItem();

        item.setId(userDto.getId());
        item.setUsername(userDto.getUsername());
        item.setMemberId(userDto.getMemberId());
        item.setIsDeleted(userDto.isDeleted());
        item.setBalance(userDto.getBalance());

        return item;
    }
}
