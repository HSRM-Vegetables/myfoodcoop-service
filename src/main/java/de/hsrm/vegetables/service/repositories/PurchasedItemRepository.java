package de.hsrm.vegetables.service.repositories;

import de.hsrm.vegetables.service.domain.dto.PurchasedItemDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchasedItemRepository extends JpaRepository<PurchasedItemDto, Long> {

}
