package de.hsrm.vegetables.service.services;

import de.hsrm.vegetables.service.domain.dto.UserDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.BadRequestError;
import de.hsrm.vegetables.service.repositories.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class UserService {

    @NonNull
    private final UserRepository userRepository;

    @NonNull
    private final PasswordEncoder passwordEncoder;

    public UserDto register(String username, String email, String memberId, String password) {

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestError("email is already in use", ErrorCode.EMAIL_IN_USE);
        }

        if (userRepository.existsByUsername(username)) {
            throw new BadRequestError("username is already in use", ErrorCode.USERNAME_IN_USE);
        }

        if (userRepository.existsByMemberId(memberId)) {
            throw new BadRequestError("memberId is already in use", ErrorCode.MEMBER_ID_IN_USE);
        }

        UserDto user = new UserDto();
        user.setUsername(username);
        user.setEmail(email);
        user.setMemberId(memberId);
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);
    }
}
