package de.hsrm.vegetables.my_food_coop_service.services;

import de.hsrm.vegetables.my_food_coop_service.domain.dto.UserDto;
import de.hsrm.vegetables.my_food_coop_service.exception.ErrorCode;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.http.BadRequestError;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.http.NotFoundError;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.http.UnauthorizedError;
import de.hsrm.vegetables.my_food_coop_service.model.BalanceChangeType;
import de.hsrm.vegetables.my_food_coop_service.model.DeleteFilter;
import de.hsrm.vegetables.my_food_coop_service.model.Role;
import de.hsrm.vegetables.my_food_coop_service.repositories.UserRepository;
import de.hsrm.vegetables.my_food_coop_service.security.JwtUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
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

    @NonNull
    private final BalanceHistoryService balanceHistoryService;

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
        user.setBalance(0f);

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

        if (user.isDeleted()) {
            throw new BadRequestError("User is already deleted", ErrorCode.USER_IS_DELETED);
        }

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

        if (user.isDeleted()) {
            throw new BadRequestError("Cannot add a role to a deleted user", ErrorCode.USER_IS_DELETED);
        }

        List<Role> roles = user.getRoles();
        if (roles.contains(role)) {
            throw new BadRequestError("User already has the role " + role, ErrorCode.USER_ALREADY_HAS_ROLE);
        }

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

        if (!roles.contains(role)) {
            throw new NotFoundError("User does not has the role" + role, ErrorCode.USER_DOESNT_HAS_ROLE);
        }

        if (role == Role.ADMIN && userRepository.countByRoles(Role.ADMIN) == 1) {
            throw new BadRequestError("Can't delete role " + role + ": User " + user.getUsername() + " is the last admin", ErrorCode.USER_IS_LAST_ADMIN);
        }

        roles.remove(role);
        user.setRoles(roles);
        return userRepository.save(user);
    }

    /**
     * Returns a page of users. Returns a page with all elements if offset is null.
     * deleteFilter controls how deleted users are treated:
     * <p>
     * OMIT: Only users which haven't been deleted will be included
     * INCLUDE: Deleted and not deleted users will be returned
     * ONLY: Only return deleted users
     *
     * @param deleteFilter How to treat deleted users
     * @param offset Pagination offset (first element in returned page)
     * @param limit Pagination limit (number of elements in returned page)
     * @return A list of users
     */
    public Page<UserDto> getAll(DeleteFilter deleteFilter, Integer offset, Integer limit) {
        Pageable pageable = (offset == null) ? Pageable.unpaged() : PageRequest.of(offset / limit, limit);

        return switch (deleteFilter) {
            case OMIT -> userRepository.findByIsDeleted(false, pageable);
            case ONLY -> userRepository.findByIsDeleted(true, pageable);
            case INCLUDE -> userRepository.findAll(pageable);
        };
    }

    /**
     * Updates data of a User.
     *
     * @param userId   Id of the User to update
     * @param memberId memberId of the User to update
     * @param email    email-Address of the user to update
     * @param password password of the user to update
     * @return The updated user
     */
    public UserDto update(String userId, String memberId, String email, String password) {
        UserDto userDto = userRepository.findById(userId);

        if (userDto == null) {
            throw new NotFoundError("No User found with this id", ErrorCode.NO_USER_FOUND);
        }

        if (userDto.isDeleted()) {
            throw new BadRequestError("Cannot update user, User is deleted", ErrorCode.USER_IS_DELETED);
        }

        boolean changed = false;

        if (memberId != null) {
            userDto.setMemberId(memberId);
            changed = true;
        }

        if (email != null) {
            userDto.setEmail(email);
            changed = true;
        }

        if (password != null) {
            userDto.setPassword(passwordEncoder.encode(password));
            changed = true;
        }

        if (changed) {
            userDto = userRepository.save(userDto);
        }
        return userDto;
    }

    /**
     * Reduces the amount of money a given user has.
     * <p>
     * Throws a NotFoundError if the user wasn't found
     *
     * @param userDto The user to withdraw the money from
     * @param amount  The amount to withdraw
     * @param saveBalanceChange Whether to log the withdraw with a balance history item
     * @return
     */
    public UserDto withdraw(UserDto userDto, Float amount, boolean saveBalanceChange) {
        userDto.setBalance(userDto.getBalance() - amount);

        if (saveBalanceChange) {
            balanceHistoryService.saveBalanceChange(userDto, OffsetDateTime.now(), null,
                    BalanceChangeType.WITHDRAW, amount);
        }

        return userRepository.save(userDto);
    }

    /**
     * Increases the amount of money a given user has.
     * <p>
     * Throws a NotFoundError if the user wasn't found
     *
     * @param userDto The user to topuup the money from
     * @param amount  The amount to topup
     * @return
     */
    public UserDto topup(UserDto userDto, Float amount) {
        userDto.setBalance(userDto.getBalance() + amount);

        balanceHistoryService.saveBalanceChange(userDto, OffsetDateTime.now(), null,
                BalanceChangeType.TOPUP, amount);

        return userRepository.save(userDto);
    }

    /**
     * Updates a users balance.
     * If no balance for the given name was found, a new entry in the database will be created, no error will be thrown
     *
     * @param userDto
     * @param amount
     * @return
     */
    public UserDto setBalance(UserDto userDto, Float amount) {
        userDto.setBalance(amount);

        balanceHistoryService.saveBalanceChange(userDto, OffsetDateTime.now(), null,
                BalanceChangeType.SET, amount);

        return userRepository.save(userDto);
    }

}
