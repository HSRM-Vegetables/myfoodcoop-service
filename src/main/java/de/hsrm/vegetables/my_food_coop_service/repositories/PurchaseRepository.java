package de.hsrm.vegetables.my_food_coop_service.repositories;

import de.hsrm.vegetables.my_food_coop_service.domain.dto.PurchaseDto;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.UserDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<PurchaseDto, Long> {

    List<PurchaseDto> findAllByUserDto(UserDto userDto);

    PurchaseDto findById(String id);

    List<PurchaseDto> findAllByCreatedOnBetween(OffsetDateTime fromDate, OffsetDateTime toDate);

}
