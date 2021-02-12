package karate.balance;

import com.intuit.karate.junit5.Karate;
import de.hsrm.vegetables.my_food_coop_service.Application;
import de.hsrm.vegetables.my_food_coop_service.model.Role;
import karate.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

@SpringBootTest(
        webEnvironment = WebEnvironment.DEFINED_PORT,
        classes = Application.class
)
@TestPropertySource(
        properties = {
                "server.port: 8090",
                "vegetables.jwt.secret: THIS:IS:A:JWT:SECRET",
                "vegetables.jwt.lifetime: 60000",
                "vegetables.jwt.refreshLifetime: 60000"
        }
)
@DirtiesContext
class BalanceTest extends BaseTest {

    @BeforeEach
    protected void setup() {
        super.setup();

        // Create test user for balance history tests
        addUser("member2", TEST_USER_PASSWORD, 500f, List.of(Role.MEMBER));
    }

    @Karate.Test
    Karate testAll() {
        return Karate.run()
                .relativeTo(getClass());
    }
}