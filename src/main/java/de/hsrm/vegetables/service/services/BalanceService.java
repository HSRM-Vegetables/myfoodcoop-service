package de.hsrm.vegetables.service.services;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceChangeType;
import de.hsrm.vegetables.service.domain.dto.BalanceHistoryItemDto;
import de.hsrm.vegetables.service.domain.dto.UserDto;
import de.hsrm.vegetables.service.repositories.BalanceHistoryItemRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired})) // Does magic to autowire all @NonNull fields
public class BalanceService {

    @NonNull
    private final BalanceHistoryItemRepository balanceHistoryItemRepository;

    /**
     * Find balance history items for a user within the specified date range
     *
     * @param userDto The user who created the balance history items
     * @param fromDate Start of time window of the balance history item list
     * @param toDate End of time window of the balance history item list
     * @return A list of balance history items created by the given user
     */
    public Page<BalanceHistoryItemDto> findAllByUserDtoAndCreatedOnBetween(
            UserDto userDto, OffsetDateTime fromDate, OffsetDateTime toDate, Pageable pageable) {

        return balanceHistoryItemRepository.findAllByUserDtoAndCreatedOnBetween(
                userDto, fromDate, toDate, pageable);
    }

    /**
     * Create and save a balance history item
     *
     * @param userDto The changed balance the balance history item refers to
     * @param balanceChangeType The type of balance change (TOPUP, WITHDRAW, etc.)
     * @param amount The amount the balance was changed by or changed to
     */
    public void saveBalanceChange(UserDto userDto, BalanceChangeType balanceChangeType, float amount) {
        BalanceHistoryItemDto balanceHistoryItem = new BalanceHistoryItemDto();

        balanceHistoryItem.setUserDto(userDto);
        balanceHistoryItem.setPurchase(null);
        balanceHistoryItem.setBalanceChangeType(balanceChangeType);
        balanceHistoryItem.setAmount(amount);

        balanceHistoryItemRepository.save(balanceHistoryItem);
    }
}
