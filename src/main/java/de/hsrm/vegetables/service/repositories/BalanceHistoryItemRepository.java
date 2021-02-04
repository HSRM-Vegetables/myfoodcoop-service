package de.hsrm.vegetables.service.repositories;

import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.domain.dto.BalanceHistoryItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface BalanceHistoryItemRepository extends JpaRepository<BalanceHistoryItemDto, Long> {

    Page<BalanceHistoryItemDto> findAllByBalanceDtoAndCreatedOnBetween(
            BalanceDto balanceDto, OffsetDateTime fromDate, OffsetDateTime toDate, Pageable pageable);
}
