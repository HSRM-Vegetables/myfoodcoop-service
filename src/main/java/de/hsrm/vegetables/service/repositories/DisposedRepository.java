package de.hsrm.vegetables.service.repositories;

import de.hsrm.vegetables.service.domain.dto.DisposedDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisposedRepository extends JpaRepository<DisposedDto, Long> {

}
