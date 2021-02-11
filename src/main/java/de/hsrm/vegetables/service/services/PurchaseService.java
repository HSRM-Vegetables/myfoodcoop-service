package de.hsrm.vegetables.service.services;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceChangeType;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.CartItem;
import de.hsrm.vegetables.service.domain.dto.*;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.InternalError;
import de.hsrm.vegetables.service.exception.errors.http.NotFoundError;
import de.hsrm.vegetables.service.repositories.PurchaseRepository;
import de.hsrm.vegetables.service.repositories.PurchasedItemRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class PurchaseService {

    @NonNull
    private final PurchaseRepository purchaseRepository;

    @NonNull
    private final PurchasedItemRepository purchasedItemRepository;

    @NonNull
    private final BalanceService balanceService;


    /**
     * Purchase items
     *
     * @param userDto The user who purchased the items
     * @param stockItems The StockDto that were purchased
     * @param cartItems  The amounts of the items purchased
     * @return The completed purchase
     */
    public PurchaseDto purchaseItems(UserDto userDto, List<StockDto> stockItems, List<CartItem> cartItems, Float totalPrice, Float totalVat) {
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
                    purchasedItemDto.setVat(associatedStockDto.get()
                            .getVat());

                    return purchasedItemDto;
                })
                .map(purchasedItemRepository::save)
                .collect(Collectors.toList());

        PurchaseDto purchaseDto = new PurchaseDto();
        purchaseDto.setTotalPrice(totalPrice);
        purchaseDto.setPurchasedItems(purchasedItems);
        purchaseDto.setUserDto(userDto);
        purchaseDto.setTotalVat(totalVat);

        PurchaseDto savedPurchaseDto = purchaseRepository.save(purchaseDto);

        balanceService.saveBalanceChange(userDto, purchaseDto.getCreatedOn(),
                savedPurchaseDto, BalanceChangeType.PURCHASE, purchaseDto.getTotalPrice());

        return savedPurchaseDto;
    }

    /**
     * Find multiple purchases by name
     *
     * @param userDto The user who purchased the items
     * @return A list of purchases made by the given user
     */
    public List<PurchaseDto> getPurchases(UserDto userDto) {
        return purchaseRepository.findAllByUserDto(userDto);
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
     * Find all purchases between fromDate and toDate
     *
     * @param fromDateConverted start of time window of the purchase list
     * @param toDateConverted   end of time window of the purchase list
     * @return A list of purchases in the given time
     */
    public List<PurchaseDto> findAllByCreatedOnBetween(OffsetDateTime fromDateConverted, OffsetDateTime toDateConverted) {
        return purchaseRepository.findAllByCreatedOnBetween(fromDateConverted, toDateConverted);
    }
}
