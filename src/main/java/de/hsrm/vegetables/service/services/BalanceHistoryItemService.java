package de.hsrm.vegetables.service.services;

import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.domain.dto.BalanceHistoryItemDto;
import de.hsrm.vegetables.service.repositories.BalanceHistoryItemRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class BalanceHistoryItemService {

    @NonNull
    private final BalanceHistoryItemRepository balanceHistoryItemRepository;

    /**
     * Find multiple balance history items by name
     *
     * @param balanceDto The balance of the user who created the balance history items
     * @return A list of balance history items created by the given user
     */
    public List<BalanceHistoryItemDto> getBalanceHistoryItems(BalanceDto balanceDto) {
        return balanceHistoryItemRepository.findAllByBalanceDto(balanceDto);
    }

    /**
     * Find all balance history items between fromDate and toDate
     *
     * @param fromDateConverted start of time window of the balance history item list
     * @param toDateConverted end of time window of the balance history item list
     * @return A list of balance history items in the given time
     */
    public List<BalanceHistoryItemDto> findAllByCreatedOnBetween(OffsetDateTime fromDateConverted, OffsetDateTime toDateConverted){
        return balanceHistoryItemRepository.findAllByCreatedOnBetween(fromDateConverted, toDateConverted);
    }
}
