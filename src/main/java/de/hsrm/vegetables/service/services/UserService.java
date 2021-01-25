package de.hsrm.vegetables.service.services;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.DeleteFilter;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.Role;
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

import java.util.List;

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

        if (email != null && email.length() > 0 && userRepository.existsByEmail(email)) {
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
        user.setEmail(email == null || email.length() <= 0 ? null : email);
        user.setMemberId(memberId);
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);
    }

    public String generateToken(UserDto user, String password) {

        if (!passwordsMatch(user, password)) {
            throw new UnauthorizedError("Username or password incorrect", ErrorCode.USERNAME_OR_PASSWORD_WRONG);
        }

        return JwtUtil.generateToken(user.getUsername(), user.getId(), user.getRoles(), jwtLifetime, jwtSecret);
    }

    public String generateToken(UserDto userDto) {
        return JwtUtil.generateToken(userDto.getUsername(), userDto.getId(), userDto.getRoles(), jwtLifetime, jwtSecret);
    }

    public UserDto getUserById(String id) {
        UserDto user = userRepository.findById(id);

        if (user == null) {
            throw new NotFoundError("No user found with given id " + id, ErrorCode.NO_USER_FOUND);
        }

        return user;
    }

    public UserDto getUserByUsername(String username) {
        UserDto user = userRepository.findByUsername(username);

        if (user == null) {
            throw new NotFoundError("No user found with given username " + username, ErrorCode.NO_USER_FOUND);
        }

        return user;
    }


    public void softDeleteUser(String id) {
        UserDto user = getUserById(id);
        user.setDeleted(true);
        userRepository.save(user);
    }

    public boolean isDeleted(String id) {
        UserDto user = getUserById(id);
        return user.isDeleted();
    }

    private boolean passwordsMatch(UserDto user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    /**
     * Adds a role to a User
     *
     * @param id   unique id of the user the role needs to be added to
     * @param role to be added to the user
     * @return current user data
     */
    public UserDto addRole(String id, Role role) {
        UserDto user = getUserById(id);
        if (user == null) {
            throw new NotFoundError("No user found with given id " + id, ErrorCode.NO_USER_FOUND);
        }

        List<Role> roles = user.getRoles();
        if (roles.contains(role))
            throw new BadRequestError("User already has the role " + role, ErrorCode.USER_ALREADY_HAS_ROLE);

        roles.add(role);
        user.setRoles(roles);
        return userRepository.save(user);
    }

    /**
     * Removes a role from the user
     *
     * @param id   unique id of the user the role needs to be removed from
     * @param role to be removed from the user
     * @return current user data
     */
    public UserDto deleteRole(String id, Role role) {
        UserDto user = getUserById(id);
        if (user == null) {
            throw new NotFoundError("No user found with given id " + id, ErrorCode.NO_USER_FOUND);
        }
        List<Role> roles = user.getRoles();

        if (!roles.contains(role))
            throw new NotFoundError("User does not has the role" + role, ErrorCode.USER_DOESNT_HAS_ROLE);

        if(role == Role.ADMIN && this.getAllByRole(Role.ADMIN).size() == 1)
            throw new BadRequestError("Can't delete role " + role + ": User " + user.getUsername() + "is the last admin", ErrorCode.USER_IS_LAST_ADMIN);

        roles.remove(role);
        user.setRoles(roles);
        return userRepository.save(user);
    }

    public List<UserDto> getAll(DeleteFilter deleteFilter) {
        return switch (deleteFilter) {
            case OMIT -> userRepository.findByIsDeleted(false);
            case ONLY -> userRepository.findByIsDeleted(true);
            case INCLUDE -> userRepository.findAll();
        };
    }

    public List<UserDto> getAllByRole(Role role) {
        return userRepository.findByRoles(role);
    }

}
