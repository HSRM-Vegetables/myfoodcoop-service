package de.hsrm.vegetables.service.repositories;

import de.hsrm.vegetables.service.domain.dto.ExampleDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Example repository to show how hibernate works
 *
 * Delete me
 */

@Repository
public interface ExampleRepository extends JpaRepository<ExampleDto, Long> {
    // Magic ;)
}
