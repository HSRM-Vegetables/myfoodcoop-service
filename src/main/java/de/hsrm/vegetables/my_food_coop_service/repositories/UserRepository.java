package de.hsrm.vegetables.my_food_coop_service.repositories;

import de.hsrm.vegetables.my_food_coop_service.domain.dto.UserDto;
import de.hsrm.vegetables.my_food_coop_service.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserDto, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByMemberId(String memberId);

    UserDto findByUsername(String username);

    UserDto findById(String id);

    Page<UserDto> findByIsDeleted(boolean isDeleted, Pageable pageable);

    long countByRoles(Role role);

}
