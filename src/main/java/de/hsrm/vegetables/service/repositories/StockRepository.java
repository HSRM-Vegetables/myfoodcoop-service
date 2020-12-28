package de.hsrm.vegetables.service.repositories;

import de.hsrm.vegetables.service.domain.dto.StockDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<StockDto, Long> {

    List<StockDto> findByIsDeleted(boolean isDeleted);

    StockDto findById(String id);

}
