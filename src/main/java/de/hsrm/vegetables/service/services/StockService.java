package de.hsrm.vegetables.service.services;

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
}
