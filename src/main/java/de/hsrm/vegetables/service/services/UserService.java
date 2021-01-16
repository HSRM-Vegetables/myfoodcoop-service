package de.hsrm.vegetables.service.services;

import de.hsrm.vegetables.service.domain.dto.UserDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.BadRequestError;
import de.hsrm.vegetables.service.exception.errors.http.NotFoundError;
import de.hsrm.vegetables.service.exception.errors.http.UnauthorizedError;
import de.hsrm.vegetables.service.repositories.UserRepository;
import de.hsrm.vegetables.service.security.JwtUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class UserService {

    @NonNull
    private final UserRepository userRepository;

    @NonNull
    private final PasswordEncoder passwordEncoder;

    @Value("${vegetables.jwt.lifetime}")
    private Integer jwtLifetime;

    @Value("${vegetables.jwt.secret}")
    private String jwtSecret;

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

    public String generateToken(String username, String password) {
        UserDto user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UnauthorizedError("Username or password incorrect", ErrorCode.USERNAME_OR_PASSWORD_WRONG);
        }

        if (!passwordsMatch(user, password)) {
            throw new UnauthorizedError("Username or password incorrect", ErrorCode.USERNAME_OR_PASSWORD_WRONG);
        }

        return JwtUtil.generateToken(user.getUsername(), user.getId(), jwtLifetime, jwtSecret);
    }

    public UserDto getUserById(String id) {
        UserDto user = userRepository.findById(id);

        if (user == null) {
            throw new NotFoundError("No user found with given id " + id, ErrorCode.NO_USER_FOUND);
        }

        return user;
    }


    private boolean passwordsMatch(UserDto user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

}
