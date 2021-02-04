package de.hsrm.vegetables.service.services;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceChangeType;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.CartItem;
import de.hsrm.vegetables.service.domain.dto.*;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.InternalError;
import de.hsrm.vegetables.service.exception.errors.http.NotFoundError;
import de.hsrm.vegetables.service.repositories.BalanceHistoryItemRepository;
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
    private final BalanceHistoryItemRepository balanceHistoryItemRepository;


    /**
     * Purchase items
     *
     * @param balanceDto The balance of the user who purchased the items
     * @param stockItems The StockDto that were purchased
     * @param cartItems  The amounts of the items purchased
     * @return The completed purchase
     */
    public PurchaseDto purchaseItems(BalanceDto balanceDto, List<StockDto> stockItems, List<CartItem> cartItems, Float totalPrice, Float totalVat) {
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
        purchaseDto.setBalanceDto(balanceDto);
        purchaseDto.setTotalVat(totalVat);

        BalanceHistoryItemDto balanceHistoryItemDto = new BalanceHistoryItemDto();
        balanceHistoryItemDto.setId(purchaseDto.getId());
        balanceHistoryItemDto.setBalanceDto(balanceDto);
        balanceHistoryItemDto.setCreatedOn(purchaseDto.getCreatedOn());
        balanceHistoryItemDto.setBalanceChangeType(BalanceChangeType.PURCHASE);
        balanceHistoryItemDto.setAmount(purchaseDto.getTotalPrice());
        balanceHistoryItemRepository.save(balanceHistoryItemDto);

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
     * Find all purchases with a user's balance that were created between fromDate and toDate.
     *
     * @param balanceDto The balance of the user who purchased the item
     * @param fromDateConverted start of time window of the purchase list
     * @param toDateConverted end of time window of the purchase list
     * @return A list of purchases by the given balance and in the given time
     */
    public List<PurchaseDto> findAllByBalanceDtoAndCreatedOnBetween(
            BalanceDto balanceDto, OffsetDateTime fromDateConverted, OffsetDateTime toDateConverted) {

        return purchaseRepository.findAllByBalanceDtoAndCreatedOnBetween(balanceDto, fromDateConverted, toDateConverted);
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
