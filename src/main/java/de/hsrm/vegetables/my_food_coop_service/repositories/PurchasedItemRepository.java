package de.hsrm.vegetables.my_food_coop_service.repositories;

import de.hsrm.vegetables.my_food_coop_service.domain.dto.PurchasedItemDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchasedItemRepository extends JpaRepository<PurchasedItemDto, Long> {

}
