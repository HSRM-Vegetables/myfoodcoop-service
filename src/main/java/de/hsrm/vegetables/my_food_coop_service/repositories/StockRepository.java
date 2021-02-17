package de.hsrm.vegetables.my_food_coop_service.repositories;

import de.hsrm.vegetables.my_food_coop_service.domain.dto.StockDto;
import de.hsrm.vegetables.my_food_coop_service.model.StockStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<StockDto, Long> {

    Page<StockDto> findAll(Pageable pageable);

    Page<StockDto> findByIsDeleted(boolean isDeleted, Pageable pageable);

    StockDto findById(String id);

    Page<StockDto> findByStockStatusIn(List<StockStatus> stockStatus, Pageable pageable);

    Page<StockDto> findByStockStatusInAndIsDeleted(List<StockStatus> stockStatus, boolean isDeleted, Pageable pageable);

}
