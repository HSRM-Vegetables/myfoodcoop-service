package de.hsrm.vegetables.service.repositories;

import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.domain.dto.TransactionDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionDto, Long> {

    List<TransactionDto> findAllByBalanceDto(BalanceDto balanceDto);

    List<TransactionDto> findAllByCreatedOnBetween(OffsetDateTime fromDate, OffsetDateTime toDate);
}
