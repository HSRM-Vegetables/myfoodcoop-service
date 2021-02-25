package de.hsrm.vegetables.my_food_coop_service.repositories;

import de.hsrm.vegetables.my_food_coop_service.domain.dto.StockDto;
import de.hsrm.vegetables.my_food_coop_service.model.StockStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<StockDto, Long> {

    @Override
    @NonNull
    Page<StockDto> findAll(@NonNull Pageable pageable);

    Page<StockDto> findByIsDeleted(boolean isDeleted, @NonNull Pageable pageable);

    StockDto findById(String id);

    Page<StockDto> findByStockStatusIn(List<StockStatus> stockStatus, @NonNull Pageable pageable);

    Page<StockDto> findByStockStatusInAndIsDeleted(List<StockStatus> stockStatus, boolean isDeleted, @NonNull Pageable pageable);

}
