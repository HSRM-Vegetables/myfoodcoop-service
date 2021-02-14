package de.hsrm.vegetables.my_food_coop_service.repositories;

import de.hsrm.vegetables.my_food_coop_service.domain.dto.BalanceHistoryItemDto;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface BalanceHistoryItemRepository extends JpaRepository<BalanceHistoryItemDto, Long> {

    List<BalanceHistoryItemDto> findAllByUserDtoAndCreatedOnBetween(
            UserDto userDto, OffsetDateTime fromDate, OffsetDateTime toDate);

    Page<BalanceHistoryItemDto> findAllByUserDtoAndCreatedOnBetween(
            UserDto userDto, OffsetDateTime fromDate, OffsetDateTime toDate, Pageable pageable);
}
