package de.hsrm.vegetables.service.repositories;

import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.domain.dto.BalanceHistoryItemDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface BalanceHistoryItemRepository extends JpaRepository<BalanceHistoryItemDto, Long> {

    List<BalanceHistoryItemDto> findAllByBalanceDto(BalanceDto balanceDto);

    List<BalanceHistoryItemDto> findAllByCreatedOnBetween(OffsetDateTime fromDate, OffsetDateTime toDate);
}
