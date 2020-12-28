package karate.stock;

import com.intuit.karate.junit5.Karate;
import de.hsrm.vegetables.service.Application;
import de.hsrm.vegetables.service.domain.dto.StockDto;
import de.hsrm.vegetables.service.repositories.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
                "server.port: 8090"
        }
)
@DirtiesContext
@AutoConfigureMockMvc
public class StockTest {

    @Autowired
    private StockRepository stockRepository;

    @AfterEach
    void cleanUp() {
        List<StockDto> stockList = stockRepository.findAll();
        stockList.forEach(stock -> stockRepository.delete(stock));
    }

    @Karate.Test
    Karate testAll() {
        return Karate.run()
                     .relativeTo(getClass());
    }
}
