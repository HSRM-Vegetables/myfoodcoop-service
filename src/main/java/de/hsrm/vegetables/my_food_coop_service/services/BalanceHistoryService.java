package de.hsrm.vegetables.my_food_coop_service.services;

import de.hsrm.vegetables.my_food_coop_service.Util;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.BalanceHistoryItemDto;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.PurchaseDto;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.UserDto;
import de.hsrm.vegetables.my_food_coop_service.model.BalanceChangeType;
import de.hsrm.vegetables.my_food_coop_service.repositories.BalanceHistoryItemRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired})) // Does magic to autowire all @NonNull fields
public class BalanceHistoryService {

    @NonNull
    private final BalanceHistoryItemRepository balanceHistoryItemRepository;

    /**
     * Find a page of balance history items for a user within the specified date range.
     * Returns a page with all elements if offset is null.
     *
     * @param userDto  The user who created the balance history items
     * @param fromDate Start of time window of the balance history item list
     * @param toDate   End of time window of the balance history item list
     * @param offset   Pagination offset (first element in returned page)
     * @param limit    Pagination limit (number of elements in returned page)
     * @return A page of balance history items created by the given user
     */
    public Page<BalanceHistoryItemDto> getBalanceHistoryBetweenDates(
            UserDto userDto, LocalDate fromDate, LocalDate toDate, Integer offset, Integer limit) {

        Util.checkDateRange(fromDate, toDate);

        OffsetDateTime fromDateConverted = OffsetDateTime.of(fromDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime toDateConverted = OffsetDateTime.of(toDate, LocalTime.MAX, ZoneOffset.UTC);

        Pageable pageable = (offset == null) ? Pageable.unpaged() : PageRequest.of(offset / limit, limit);

        return balanceHistoryItemRepository.findAllByUserDtoAndCreatedOnBetween(
                userDto, fromDateConverted, toDateConverted, pageable);
    }

    /**
     * Create and save a balance history item
     *
     * @param userDto           The changed balance the balance history item refers to
     * @param createdOn         Time of the balance change
     * @param purchaseDto       Associated purchase, if balance change resulted from such a one
     * @param balanceChangeType The type of balance change (TOPUP, WITHDRAW, etc.)
     * @param amount            The amount the balance was changed by or changed to
     */
    public void saveBalanceChange(UserDto userDto, OffsetDateTime createdOn, PurchaseDto purchaseDto,
                                  BalanceChangeType balanceChangeType, float amount) {

        BalanceHistoryItemDto balanceHistoryItem = new BalanceHistoryItemDto();

        balanceHistoryItem.setUserDto(userDto);
        balanceHistoryItem.setCreatedOn(createdOn);
        balanceHistoryItem.setPurchase(purchaseDto);
        balanceHistoryItem.setBalanceChangeType(balanceChangeType);
        balanceHistoryItem.setAmount(amount);

        balanceHistoryItemRepository.save(balanceHistoryItem);
    }
}
