package karate.helloWorld;

import com.intuit.karate.junit5.Karate;
import de.hsrm.vegetables.service.Application;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        webEnvironment = WebEnvironment.DEFINED_PORT,
        classes = Application.class
)
@TestPropertySource(
        properties = {
                "server.port: 8090"
        }
)
class HelloWorldTest {

    @Karate.Test
    Karate testAll() {
        return Karate.run()
                     .relativeTo(getClass());
    }
}