package de.hsrm.vegetables.service.repositories;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.StockStatus;
import de.hsrm.vegetables.service.domain.dto.StockDto;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<StockDto, Long> {

    List<StockDto> findByIsDeleted(boolean isDeleted, Sort sort);

    StockDto findById(String id);

    List<StockDto> findByStockStatusIn(List<StockStatus> stockStatus, Sort sort);

    List<StockDto> findByStockStatusInAndIsDeleted(List<StockStatus> stockStatus, boolean isDeleted, Sort sort);

}