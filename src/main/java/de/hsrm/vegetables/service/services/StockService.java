package de.hsrm.vegetables.service.services;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.UnitType;
import de.hsrm.vegetables.service.domain.dto.ExampleDto;
import de.hsrm.vegetables.service.domain.dto.StockDto;
import de.hsrm.vegetables.service.repositories.StockRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class StockService {
    @NonNull
    private final StockRepository stockRepository;

    public List<StockDto> getAll() {
        return stockRepository.findByIsDeleted(false);
    }

    public StockDto getById(String id) {
        return stockRepository.findById(id);
    }
    public StockDto addStock(String name, UnitType unitType, Float quantity, Float pricePerUnit, String description) {
        StockDto stockDto = new StockDto();
        stockDto.setName(name);
        stockDto.setUnitType(unitType);
        stockDto.setQuantity(quantity);
        stockDto.setPricePerUnit(pricePerUnit);
        stockDto.setDescription(description);
        stockRepository.save(stockDto);
        return stockDto;
    }
}
