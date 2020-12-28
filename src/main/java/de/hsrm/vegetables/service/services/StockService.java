package de.hsrm.vegetables.service.services;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.CartItem;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            throw new NotFoundError("No item in the stock was found with id " + id, ErrorCode.NO_STOCK_ITEM_FOUND);
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

    /**
     * Reduces quantity in stock for each item in items and returns the total price
     * @param items All items to reduce quanitity for
     * @return The price for all these items
     */
    public Float purchase(List<CartItem> items) {
        // Check that all id's are unique
        items.stream().forEach(item -> {
            if (countItemsWithId(items, item.getId()) > 1 ) {
                throw new BadRequestError("Cannot have multiple cart items with same id", ErrorCode.MULTIPLE_ITEMS_WITH_SAME_ID);
            }
        });

        List<StockDto> newStockItems = new ArrayList<>();

        // Sum up price and reduce items in stock
        Float totalPrice =  items.stream().map(item -> {
            StockDto stockDto = getById(item.getId());

            if (stockDto.getUnitType().equals(UnitType.PIECE) && item.getAmount() % 1 != 0) {
                throw new BadRequestError("Cannot purchase item with UnitType PIECE and a fractional quantity", ErrorCode.NO_FRACTIONAL_QUANTITY);
            }

            if (stockDto.isDeleted()) {
                throw new BadRequestError("Cannot buy deleted item " + stockDto.getId(), ErrorCode.CANNOT_PURCHASE_DELETED_ITEM);
            }

            float price = stockDto.getPricePerUnit() * item.getAmount();

            stockDto.setQuantity(stockDto.getQuantity() - item.getAmount());

            // save the new stock items so we can commit changes after we've checked all items in the list
            newStockItems.add(stockDto);

            return round(price, 2);
        })
        .reduce(0f, Float::sum);

        // Commit changes to repository
        newStockItems.forEach(stockRepository::save);

        return round(totalPrice, 2);
    }

    /**
     * counts how often an item with a given id exists in the list
     * @param items The list of items to check in
     * @param id The id to check for
     * @return How often the id exists in the list
     */
    private int countItemsWithId(List<CartItem> items, String id) {
        return items.stream().filter(item -> item.getId().equals(id)).collect(Collectors.toList()).size();
    }

    /**
     * Rounds a float to a set amount of decimal places
     * @param value The number to round
     * @param places How many decimal places to round to
     * @return The rounded number with places decimal places
     */
    private static float round(float value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

}
