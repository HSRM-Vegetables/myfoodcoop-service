package de.hsrm.vegetables.service.repositories;

import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.domain.dto.PurchaseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<PurchaseDto, Long> {

    List<PurchaseDto> findAllByBalanceDto(BalanceDto balanceDto);

    List<PurchaseDto> findAllByBalanceDtoAndCreatedOnBetween(BalanceDto balanceDto, OffsetDateTime fromDate, OffsetDateTime toDate);

    PurchaseDto findById(String id);

    List<PurchaseDto> findAllByCreatedOnBetween(OffsetDateTime fromDate, OffsetDateTime toDate);

}
