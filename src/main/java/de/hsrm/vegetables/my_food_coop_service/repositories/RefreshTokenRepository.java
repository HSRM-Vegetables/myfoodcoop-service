package de.hsrm.vegetables.my_food_coop_service.repositories;

import de.hsrm.vegetables.my_food_coop_service.domain.dto.RefreshTokenDto;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.UserDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenDto, Long> {

    // Revokes all refresh tokens for a user
    void deleteAllByUser(UserDto user);

    // Check if a refreshToken is valid
    // We only store valid refreshTokens
    boolean existsByRefreshToken(String refreshToken);

    // Revoke a single refresh token
    void deleteByRefreshToken(String refreshToken);

}
