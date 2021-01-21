package karate;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.Role;
import de.hsrm.vegetables.service.domain.dto.UserDto;
import de.hsrm.vegetables.service.services.BalanceService;
import de.hsrm.vegetables.service.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class BaseTest {

    private static final String TEST_USER_PASSWORD = "a_funny_horse**jumps_high778";

    @Autowired
    private UserService userService;

    @Autowired
    private BalanceService balanceService;

    @BeforeEach
    void setup() {
        // Create test user for each role
        addUser("member", TEST_USER_PASSWORD, 500f, List.of(Role.MEMBER));
        addUser("orderer", TEST_USER_PASSWORD, 500f, List.of(Role.MEMBER, Role.ORDERER));
        addUser("chairman", TEST_USER_PASSWORD, 500f, List.of(Role.MEMBER, Role.CHAIRMAN));
        addUser("treasurer", TEST_USER_PASSWORD, 500f, List.of(Role.MEMBER, Role.TREASURER));
    }

    private void addUser(String username, String password, Float balance, List<Role> roles) {
        UserDto user = userService.register(username, username + "@mail.com", username + "Id", password);
        roles.forEach(role -> userService.addRole(user.getId(), role));
        balanceService.upsert(username, balance);
    }

}
