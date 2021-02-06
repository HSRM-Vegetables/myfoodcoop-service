package karate;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.OriginCategory;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.Role;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.StockStatus;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.UnitType;
import de.hsrm.vegetables.service.domain.dto.UserDto;
import de.hsrm.vegetables.service.services.BalanceService;
import de.hsrm.vegetables.service.services.StockService;
import de.hsrm.vegetables.service.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public abstract class BaseTest {

    private static final String TEST_USER_PASSWORD = "a_funny_horse**jumps_high778";

    @Autowired
    private UserService userService;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private StockService stockService;

    @BeforeEach
    void setup() {
        // Create test user for each role
        addUser("member", TEST_USER_PASSWORD, 500f, List.of(Role.MEMBER));
        addUser("orderer", TEST_USER_PASSWORD, 500f, List.of(Role.MEMBER, Role.ORDERER));
        addUser("admin", TEST_USER_PASSWORD, 500f, List.of(Role.MEMBER, Role.ADMIN));
        addUser("treasurer", TEST_USER_PASSWORD, 500f, List.of(Role.MEMBER, Role.TREASURER));


        stockService.addStock("Bananas", UnitType.WEIGHT, 104.3f, 1.3f, "A description",
                true, List.of("Demeter"), OriginCategory.LOCAL, "Farmer Joe", "CargoBike Carl",
                LocalDate.now(), LocalDate.now(), StockStatus.INSTOCK, 0.19f);

        stockService.addStock("Carrots", UnitType.PIECE, 100f, 0.5f, "A description",
                true, Collections.emptyList(), OriginCategory.REGIONAL, "Farmer Joe", "CargoBike Carl",
                LocalDate.now()
                        .minusDays(5), LocalDate.now()
                        .minusDays(4), StockStatus.INSTOCK, 0.16f);

        stockService.addStock("Eggplant", UnitType.WEIGHT, 0f, 10f, "A description",
                true, List.of("Demeter"), OriginCategory.LOCAL, "Farmer Joe", "CargoBike Carl",
                LocalDate.now()
                        .minusDays(4), LocalDate.now()
                        .minusDays(3), StockStatus.INSTOCK, 0.19f);

        stockService.addStock("Kiwi", UnitType.WEIGHT, 100f, 1f, "A description",
                true, List.of("Demeter", "Test"), OriginCategory.SUPRAREGIONAL, "Farmer Joe", "CargoBike Carl",
                LocalDate.now()
                        .minusDays(3), LocalDate.now()
                        .minusDays(2), StockStatus.INSTOCK, 0.16f);

        stockService.addStock("Avocado", UnitType.PIECE, 25f, 2.7f, "A description",
                true, List.of("Demeter", "MMM", "TTT"), OriginCategory.LOCAL, "Farmer Joe", "CargoBike Carl",
                LocalDate.now()
                        .minusDays(2), LocalDate.now()
                        .minusDays(1), StockStatus.INSTOCK, 0.19f);

        stockService.addStock("Honey", UnitType.WEIGHT, 100f, 1f, "A description",
                true, List.of("AAAA"), OriginCategory.UNKNOWN, "Farmer Joe", "CargoBike Carl",
                LocalDate.now()
                        .minusDays(20), LocalDate.now()
                        .minusDays(10), StockStatus.INSTOCK, 0.19f);
    }

    private void addUser(String username, String password, Float balance, List<Role> roles) {
        UserDto user = userService.register(username, username + "@mail.com", username + "Id", password);
        roles.forEach(role -> userService.addRole(user.getId(), role));
        balanceService.upsert(username, balance);
    }

}
