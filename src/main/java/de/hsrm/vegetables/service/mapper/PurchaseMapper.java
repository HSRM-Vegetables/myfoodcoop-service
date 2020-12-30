package de.hsrm.vegetables.service.mapper;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.PurchaseHistoryItem;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.PurchaseItem;
import de.hsrm.vegetables.service.domain.dto.PurchaseDto;
import de.hsrm.vegetables.service.domain.dto.PurchasedItemDto;

import java.util.stream.Collectors;

public class PurchaseMapper {

    private PurchaseMapper() {
    }

    public static PurchaseHistoryItem purchaseDtoToPurchaseHistoryItem(PurchaseDto purchaseDto) {
        PurchaseHistoryItem purchaseHistoryItem = new PurchaseHistoryItem();
        purchaseHistoryItem.setId(purchaseDto.getId());
        purchaseHistoryItem.setTotalPrice(purchaseDto.getTotalPrice());
        purchaseHistoryItem.setCreatedOn(purchaseDto.getCreatedOn());
        purchaseHistoryItem.setItems(purchaseDto.getPurchasedItems()
                .stream()
                .map(PurchaseMapper::purchasedItemDtoToPurchaseItem)
                .collect(Collectors.toList()));
        return purchaseHistoryItem;
    }

    public static PurchaseItem purchasedItemDtoToPurchaseItem(PurchasedItemDto purchasedItemDto) {
        PurchaseItem purchaseItem = new PurchaseItem();
        purchaseItem.setId(purchasedItemDto.getStockDto()
                .getId());
        purchaseItem.setName(purchasedItemDto.getStockDto()
                .getName());
        purchaseItem.setAmount(purchasedItemDto.getAmount());
        purchaseItem.setPricePerUnit(purchasedItemDto.getPricePerUnit());
        purchaseItem.setUnitType(purchasedItemDto.getUnitType());

        return purchaseItem;
    }

}
