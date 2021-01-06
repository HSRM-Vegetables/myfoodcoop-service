package de.hsrm.vegetables.service.services;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.CartItem;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.QuantitySoldItem;
import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.domain.dto.PurchaseDto;
import de.hsrm.vegetables.service.domain.dto.PurchasedItemDto;
import de.hsrm.vegetables.service.domain.dto.StockDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.InternalError;
import de.hsrm.vegetables.service.exception.errors.http.NotFoundError;
import de.hsrm.vegetables.service.repositories.PurchaseRepository;
import de.hsrm.vegetables.service.repositories.PurchasedItemRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class PurchaseService {

    @NonNull
    private final PurchaseRepository purchaseRepository;

    @NonNull
    private final PurchasedItemRepository purchasedItemRepository;

    @NonNull
    private final StockService stockService;

    /**
     * Purchase items
     *
     * @param balanceDto The balance of the user who purchased the items
     * @param stockItems The StockDto that were purchased
     * @param cartItems  The amounts of the items purchased
     * @return The completed purchase
     */
    public PurchaseDto purchaseItems(BalanceDto balanceDto, List<StockDto> stockItems, List<CartItem> cartItems, Float totalPrice) {
        List<PurchasedItemDto> purchasedItems = cartItems
                .stream()
                .map(item -> {
                    Optional<StockDto> associatedStockDto = stockItems
                            .stream()
                            .filter(stockItem -> stockItem.getId()
                                    .equals(item.getId()))
                            .findFirst();

                    if (associatedStockDto.isEmpty()) {
                        throw new InternalError("No matching stock item was found in stockItems", ErrorCode.STOCK_DTO_NOT_FOUND);
                    }

                    PurchasedItemDto purchasedItemDto = new PurchasedItemDto();
                    purchasedItemDto.setAmount(item.getAmount());
                    purchasedItemDto.setPricePerUnit(associatedStockDto.get()
                            .getPricePerUnit());
                    purchasedItemDto.setStockDto(associatedStockDto.get());
                    purchasedItemDto.setUnitType(associatedStockDto.get()
                            .getUnitType());

                    return purchasedItemDto;
                })
                .map(purchasedItemRepository::save)
                .collect(Collectors.toList());

        PurchaseDto purchaseDto = new PurchaseDto();
        purchaseDto.setTotalPrice(totalPrice);
        purchaseDto.setPurchasedItems(purchasedItems);
        purchaseDto.setBalanceDto(balanceDto);

        return purchaseRepository.save(purchaseDto);
    }

    /**
     * Find multiple purchases by name
     *
     * @param balanceDto The balance of the user who purchased the items
     * @return A list of purchases made by the given user
     */
    public List<PurchaseDto> getPurchases(BalanceDto balanceDto) {
        return purchaseRepository.findAllByBalanceDto(balanceDto);
    }

    /**
     * Find one purchase by id
     *
     * @param id Unique id of the purchase
     * @return The whole purchase
     */
    public PurchaseDto getPurchase(String id) {
        PurchaseDto purchaseDto = purchaseRepository.findById(id);

        if (purchaseDto == null) {
            throw new NotFoundError("No purchase with id " + id + " was found", ErrorCode.NO_PURCHASE_FOUND);
        }

        return purchaseDto;
    }

    /**
     * Find multiple purchases between Dates
     *
     * @param fromDate time window from offsetDateTime where item was purchased
     * @param toDate time window to offsetDateTime where item was purchased
     * @return All purchases between fromDate and toDate
     */
    public  List<QuantitySoldItem> getSoldItems(LocalDate fromDate, LocalDate toDate) {
        // Local Dates only contain date information and are missing time information.
        // Convert the LocalDate to a timestamp with the options specified below.
        OffsetDateTime fromDateConverted = OffsetDateTime.of(fromDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime toDateConverted = OffsetDateTime.of(toDate, LocalTime.MAX, ZoneOffset.UTC);

        var purchases = purchaseRepository.findAllByCreatedOnBetween(fromDateConverted,toDateConverted);
        var purchaseQuantityByStockId = new HashMap<String, QuantitySoldItem>();

        // iterate over each purchaseItem and collect the amount each purchaseItem was purchased
        purchases.forEach(purchase -> {
            purchase.getPurchasedItems().forEach(purchaseItem -> {
                var stockId = purchaseItem.getStockDto().getId();
                QuantitySoldItem quantitySoldItem;

                // create or update Item to collect amount that was purchased
                if (purchaseQuantityByStockId.containsKey(stockId)) {
                    quantitySoldItem = purchaseQuantityByStockId.get(stockId);
                    quantitySoldItem.setQuantitySold(quantitySoldItem.getQuantitySold() + purchaseItem.getAmount());
                } else {
                    quantitySoldItem = new QuantitySoldItem();
                    quantitySoldItem.setQuantitySold(purchaseItem.getAmount());
                    quantitySoldItem.setId(stockId);
                    quantitySoldItem.setUnitType(purchaseItem.getUnitType());
                }

                purchaseQuantityByStockId.put(stockId, quantitySoldItem);
            });
        });

        // additionally collect the name of each item from the stock
        purchaseQuantityByStockId.forEach((stockId, quantitySoldItem) -> {
            var stockItem = stockService.getById(stockId);
            quantitySoldItem.setName(stockItem.getName());
        });

        return new ArrayList<>(purchaseQuantityByStockId.values());
    }

}
