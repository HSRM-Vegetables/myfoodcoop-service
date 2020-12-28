package de.hsrm.vegetables.service.services;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.UnitType;
import de.hsrm.vegetables.service.domain.dto.StockDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.BadRequestError;
import de.hsrm.vegetables.service.exception.errors.http.NotFoundError;
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

    /**
     * Returns all items currently in stock
     * Soft deleted entries will be ignored
     * @return A list of all items currently in stock
     */
    public List<StockDto> getAll() {
        return stockRepository.findByIsDeleted(false);
    }

    /**
     * Returns a single item identified by its id
     * @param id The id of the item to return
     * @return The item
     */
    public StockDto getById(String id) {
        StockDto stockDto = stockRepository.findById(id);

        if (stockDto == null) {
            throw new NotFoundError("No item in the stock was found with this id", ErrorCode.NO_STOCK_ITEM_FOUND);
        }

        return stockRepository.findById(id);
    }

    /**
     * Add a new item to the stock
     * @param name
     * @param unitType
     * @param quantity
     * @param pricePerUnit
     * @param description
     * @return The full item as saved in the database
     */
    public StockDto addStock(String name, UnitType unitType, Float quantity, Float pricePerUnit, String description) {
        if (unitType.equals(UnitType.PIECE) && quantity % 1 != 0) {
            throw new BadRequestError("Cannot have a fractional quantity with UnitType PIECE", ErrorCode.NO_FRACTIONAL_QUANTITY);
        }

        StockDto stockDto = new StockDto();
        stockDto.setName(name);
        stockDto.setUnitType(unitType);
        stockDto.setQuantity(quantity);
        stockDto.setPricePerUnit(pricePerUnit);
        stockDto.setDescription(description);
        stockRepository.save(stockDto);
        return stockDto;
    }

    /**
     * Soft deletes an item from the database
     * @param id The id of the item to delete
     */
    public void delete(String id) {
        StockDto stockDto = stockRepository.findById(id);

        if (stockDto == null) {
            throw new NotFoundError("No item in the stock was found with this id", ErrorCode.NO_STOCK_ITEM_FOUND);
        }

        stockDto.setDeleted(true);
        stockRepository.save(stockDto);
    }

    /**
     * Updates data of an item. Null values will be ignored
     * @param id
     * @param name
     * @param unitType
     * @param quantity
     * @param pricePerUnit
     * @param description
     * @return The updated item
     */
    public StockDto update(String id, String name, UnitType unitType, Float quantity, Float pricePerUnit, String description) {
        StockDto stockDto = stockRepository.findById(id);

        if (stockDto == null) {
            throw new NotFoundError("No item in the stock was found with this id", ErrorCode.NO_STOCK_ITEM_FOUND);
        }

        if (stockDto.isDeleted()) {
            throw new BadRequestError("Cannot update values of a deleted item", ErrorCode.ITEM_IS_DELETED);
        }

        boolean changed = false;

        if (name != null && name.length() > 0) {
            stockDto.setName(name);
            changed = true;
        }

        if (unitType != null) {
            stockDto.setUnitType(unitType);
            changed = true;
        }

        if (quantity != null) {
            stockDto.setQuantity(quantity);
            changed = true;
        }

        if (pricePerUnit != null) {
            stockDto.setPricePerUnit(pricePerUnit);
            changed = true;
        }

        if (description != null) {
            stockDto.setDescription(description);
            changed = true;
        }

        if (stockDto.getUnitType().equals(UnitType.PIECE) && stockDto.getQuantity() % 1 != 0) {
            throw new BadRequestError("Cannot have a fractional quantity with UnitType PIECE", ErrorCode.NO_FRACTIONAL_QUANTITY);
        }

        if (changed) {
            stockDto = stockRepository.save(stockDto);
        }

        return stockDto;
    }

}
