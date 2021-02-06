package de.hsrm.vegetables.service.services;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.*;
import de.hsrm.vegetables.service.domain.dto.DisposedDto;
import de.hsrm.vegetables.service.domain.dto.StockDto;
import de.hsrm.vegetables.service.domain.dto.UserDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.BadRequestError;
import de.hsrm.vegetables.service.exception.errors.http.InternalError;
import de.hsrm.vegetables.service.exception.errors.http.NotFoundError;
import de.hsrm.vegetables.service.repositories.DisposedRepository;
import de.hsrm.vegetables.service.repositories.StockRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class StockService {

    @NonNull
    private final StockRepository stockRepository;

    @NonNull
    private final DisposedRepository disposedRepository;

    /**
     * Returns all items currently in stock
     * deleteFilter controls how deleted entries are treated:
     * <p>
     * OMIT: Only elements which haven't been deleted will be included
     * INCLUDE: Deleted and not deleted items will be returned
     * ONLY: Only return deleted items
     *
     * @param deleteFilter How to treat deleted items
     * @return A list of stock items
     */
    public List<StockDto> getStock(DeleteFilter deleteFilter, List<StockStatus> stockFilter) {
        // No filtering by status
        if (stockFilter == null || stockFilter.isEmpty()) {
            return switch (deleteFilter) {
                case OMIT -> stockRepository.findByIsDeleted(false);
                case ONLY -> stockRepository.findByIsDeleted(true);
                case INCLUDE -> stockRepository.findAll();
            };
        }

        // No filtering by deleted but by status
        if (deleteFilter.equals(DeleteFilter.INCLUDE)) {
            return stockRepository.findByStockStatusIn(stockFilter);
        }

        // filtering by stockStatus and deleted
        return stockRepository.findByStockStatusInAndIsDeleted(stockFilter, !deleteFilter.equals(DeleteFilter.OMIT));
    }

    /**
     * Returns a single item identified by its id
     *
     * @param id The id of the item to return
     * @return The item
     */
    public StockDto getById(String id) {
        StockDto stockDto = stockRepository.findById(id);

        if (stockDto == null) {
            throw new NotFoundError("No item in the stock was found with id " + id, ErrorCode.NO_STOCK_ITEM_FOUND);
        }

        return stockDto;
    }

    /**
     * Add a new item to the stock
     *
     * @param name         Name of the item
     * @param unitType     UnitType of the item
     * @param quantity     Quantity of the item
     * @param pricePerUnit Price of the item per unit
     * @param description  Description of the item
     * @return The full item as saved in the database
     */
    public StockDto addStock(String name, UnitType unitType, Float quantity, Float pricePerUnit, String description,
                             boolean sustainablyProduced, List<String> certificates, OriginCategory originCategory,
                             String producer, String supplier, LocalDate orderDate, LocalDate deliveryDate, StockStatus stockStatus, Float vat) {
        if (unitType.equals(UnitType.PIECE) && quantity % 1 != 0) {
            throw new BadRequestError("Cannot have a fractional quantity with UnitType PIECE", ErrorCode.NO_FRACTIONAL_QUANTITY);
        }

        StockDto stockDto = new StockDto();
        stockDto.setName(name);
        stockDto.setUnitType(unitType);
        stockDto.setQuantity(quantity);
        stockDto.setPricePerUnit(pricePerUnit);
        stockDto.setDescription(description);
        stockDto.setSustainablyProduced(sustainablyProduced);
        stockDto.setCertificates(certificates);
        stockDto.setOriginCategory(originCategory);
        stockDto.setProducer(producer);
        stockDto.setSupplier(supplier);
        stockDto.setOrderDate(orderDate);
        stockDto.setDeliveryDate(deliveryDate);
        stockDto.setStockStatus(stockStatus);
        stockDto.setVat(vat);

        return stockRepository.save(stockDto);
    }

    /**
     * Soft deletes an item from the database
     *
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
     *
     * @param id           Id of the item to update
     * @param name         Name of the item
     * @param unitType     UnitType of the item
     * @param quantity     Quantity of the item
     * @param pricePerUnit Price of the item per unit
     * @param description  Description of the item
     * @return The updated item
     */
    public StockDto update(String id, String name, UnitType unitType, Float quantity, Float pricePerUnit, String description,
                           Boolean sustainablyProduced, List<String> certificates, OriginCategory originCategory,
                           String producer, String supplier, LocalDate orderDate, LocalDate deliveryDate, StockStatus stockStatus, Float vat) {
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

        if (stockStatus != null) {
            stockDto.setStockStatus(stockStatus);
            changed = true;
        }

        if (stockDto.getUnitType()
                .equals(UnitType.PIECE) && stockDto.getQuantity() % 1 != 0) {
            throw new BadRequestError("Cannot have a fractional quantity with UnitType PIECE", ErrorCode.NO_FRACTIONAL_QUANTITY);
        }

        if (sustainablyProduced != null && sustainablyProduced != stockDto.isSustainablyProduced()) {
            stockDto.setSustainablyProduced(sustainablyProduced);
            changed = true;
        }

        if (certificates != null) {
            stockDto.setCertificates(certificates);
            changed = true;
        }

        if (originCategory != null) {
            stockDto.setOriginCategory(originCategory);
            changed = true;
        }

        if (originCategory != null) {
            stockDto.setOriginCategory(originCategory);
            changed = true;
        }

        if (producer != null) {
            stockDto.setProducer(producer);
            changed = true;
        }

        if (supplier != null) {
            stockDto.setSupplier(supplier);
            changed = true;
        }

        if (orderDate != null) {
            stockDto.setOrderDate(orderDate);
            changed = true;
        }

        if (deliveryDate != null) {
            stockDto.setDeliveryDate(deliveryDate);
            changed = true;
        }

        if (vat != null) {
            stockDto.setVat(vat);
            changed = true;
        }

        if (changed) {
            stockDto = stockRepository.save(stockDto);
        }

        return stockDto;
    }

    /**
     * Reduces quantity in stock for each item in items and returns the total price
     *
     * @param items All items to reduce quanitity for
     * @return The price for all these items
     */
    public List<StockDto> purchase(List<CartItem> items) {
        // Check that all id's are unique
        items.forEach(item -> {
            if (countItemsWithId(items, item.getId()) > 1) {
                throw new BadRequestError("Cannot have multiple cart items with same id", ErrorCode.MULTIPLE_ITEMS_WITH_SAME_ID);
            }
        });

        // Sum up price and reduce items in stock
        List<StockDto> modifiedStockItems = items
                .stream()
                .map(item -> {
                    StockDto stockDto = getById(item.getId());

                    if (stockDto.getUnitType()
                            .equals(UnitType.PIECE) && item.getAmount() % 1 != 0) {
                        throw new BadRequestError("Cannot purchase item with UnitType PIECE and a fractional quantity", ErrorCode.NO_FRACTIONAL_QUANTITY);
                    }

                    if (stockDto.isDeleted()) {
                        throw new BadRequestError("Cannot buy deleted item " + stockDto.getId(), ErrorCode.CANNOT_PURCHASE_DELETED_ITEM);
                    }

                    // Check that purchased item is not out of stock
                    if (stockDto.getStockStatus()
                            .equals(StockStatus.OUTOFSTOCK)) {
                        throw new BadRequestError("Cannot purchase OUTOFSTOCK item with id " + item.getId(), ErrorCode.ITEM_OUT_OF_STOCK);
                    }

                    // Check that purchased item is not just ordered and hence not in the store yet
                    if (stockDto.getStockStatus()
                            .equals(StockStatus.ORDERED)) {
                        throw new BadRequestError("Cannot purchase ORDERED item with id " + item.getId(), ErrorCode.ITEM_NOT_IN_STOCK_YET);
                    }

                    stockDto.setQuantity(stockDto.getQuantity() - item.getAmount());

                    return stockDto;
                })
                .collect(Collectors.toList());

        // Commit changes to repository
        return modifiedStockItems.stream()
                .map(stockRepository::save)
                .collect(Collectors.toList());
    }

    public DisposedDto dispose(String stockId, UserDto userDto, float amount) {
        StockDto stockDto = getById(stockId);

        if (stockDto.isDeleted()) {
            throw new BadRequestError("Cannot dispose of deleted item " + stockDto.getId(), ErrorCode.CANNOT_DISPOSE_DELETED_ITEM);
        }

        if (stockDto.getUnitType()
                .equals(UnitType.PIECE) && amount % 1 != 0) {
            throw new BadRequestError("Cannot dispose of item with UnitType PIECE and a fractional quantity", ErrorCode.NO_FRACTIONAL_QUANTITY);
        }

        // reduce stock amount
        stockDto.setQuantity(stockDto.getQuantity() - amount);
        stockDto = stockRepository.save(stockDto);

        // create disposed Item
        DisposedDto disposedDto = new DisposedDto();
        disposedDto.setStockDto(stockDto);
        disposedDto.setAmount(amount);
        disposedDto.setVat(stockDto.getVat());
        disposedDto.setUnitType(stockDto.getUnitType());
        disposedDto.setPricePerUnit(stockDto.getPricePerUnit());
        disposedDto.setUserDto(userDto);

        return disposedRepository.save(disposedDto);
    }

    public static Float calculatePrice(List<StockDto> stockItems, List<CartItem> cartItems) {
        Float totalPrice = cartItems
                .stream()
                .map(cartItem -> {
                    Optional<StockDto> associatedStockDto = stockItems
                            .stream()
                            .filter(stockItem -> stockItem.getId()
                                    .equals(cartItem.getId()))
                            .findFirst();

                    if (associatedStockDto.isEmpty()) {
                        throw new InternalError("No matching stock item was found in stockItems", ErrorCode.STOCK_DTO_NOT_FOUND);
                    }

                    float price = associatedStockDto.get()
                            .getPricePerUnit() * cartItem.getAmount();
                    return round(price, 2);
                })
                .reduce(0f, Float::sum);
        return round(totalPrice, 2);
    }

    public static Float calculateVatAmount(List<StockDto> stockItems, List<CartItem> cartItems) {
        Float totalVat = cartItems
                .stream()
                .map(cartItem -> {
                    Optional<StockDto> associatedStockDtoOpt = stockItems
                            .stream()
                            .filter(stockItem -> stockItem.getId()
                                    .equals(cartItem.getId()))
                            .findFirst();

                    if (associatedStockDtoOpt.isEmpty()) {
                        throw new InternalError("No matching stock item was found in stockItems", ErrorCode.STOCK_DTO_NOT_FOUND);
                    }

                    StockDto associatedStockDto = associatedStockDtoOpt.get();
                    float vat = associatedStockDto.getVat();
                    return round((associatedStockDto
                            .getPricePerUnit() * cartItem.getAmount()) / (1f + vat) * vat, 2);
                })
                .reduce(0f, Float::sum);
        return round(totalVat, 2);
    }

    public static List<VatDetailItem> getVatDetails(List<StockDto> stockItems, List<CartItem> cartItems) {
        ArrayList<Float> distinctVats = new ArrayList<>();
        // Find all distinct vat rates in the stock
        stockItems.forEach(stockDto -> {
            if (!distinctVats.contains(stockDto.getVat())) {
                distinctVats.add(stockDto.getVat());
            }
        });

        return distinctVats.stream()
                .map(vat -> {
                    // get all stock with that specific vat
                    List<StockDto> stockWithSpecificVat = stockItems.stream()
                            .filter(stockDto -> stockDto.getVat()
                                    .equals(vat))
                            .collect(Collectors.toList());

                    // Find all cart items associated to these stock items
                    List<CartItem> cartWithSpecificVat = cartItems.stream()
                            .filter(cartItem -> stockWithSpecificVat.stream()
                                    .anyMatch(stockDto -> stockDto.getId()
                                            .equals(cartItem.getId()))
                            )
                            .collect(Collectors.toList());

                    // Calculate vat amount for that subset of items
                    Float vatAmount = calculateVatAmount(stockWithSpecificVat, cartWithSpecificVat);
                    VatDetailItem vatDetailItem = new VatDetailItem();
                    vatDetailItem.setVat(vat);
                    vatDetailItem.setAmount(vatAmount);
                    return vatDetailItem;
                })
                .collect(Collectors.toList());
    }

    /**
     * counts how often an item with a given id exists in the list
     *
     * @param items The list of items to check in
     * @param id    The id to check for
     * @return How often the id exists in the list
     */
    private int countItemsWithId(List<CartItem> items, String id) {
        return (int) items
                .stream()
                .filter(item -> item.getId()
                        .equals(id))
                .count();
    }

    /**
     * Rounds a float to a set amount of decimal places
     *
     * @param value  The number to round
     * @param places How many decimal places to round to
     * @return The rounded number with places decimal places
     */
    public static float round(float value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Float.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

}
