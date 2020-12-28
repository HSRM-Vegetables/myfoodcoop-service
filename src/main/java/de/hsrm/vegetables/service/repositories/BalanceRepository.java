package de.hsrm.vegetables.service.repositories;

import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalanceRepository extends JpaRepository<BalanceDto, Long> {

    // Using JPA method name derived queries: https://www.baeldung.com/spring-data-derived-queries
    List<BalanceDto> findByName(String name);

}
