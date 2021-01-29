package de.hsrm.vegetables.service.repositories;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.Role;
import de.hsrm.vegetables.service.domain.dto.UserDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserDto, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByMemberId(String memberId);

    UserDto findByUsername(String username);

    UserDto findById(String id);

    List<UserDto> findByIsDeleted(boolean isDeleted);

    long countByRoles(Role role);

}
