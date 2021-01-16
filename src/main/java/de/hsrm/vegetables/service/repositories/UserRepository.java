package de.hsrm.vegetables.service.repositories;

import de.hsrm.vegetables.service.domain.dto.UserDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserDto, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByMemberId(String memberId);

    UserDto findByUsername(String username);

}
