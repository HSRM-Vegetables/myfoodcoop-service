package de.hsrm.vegetables.service.mapper;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.PurchaseHistoryItem;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.PurchaseItem;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.QuantitySoldItem;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.VatDetailItem;
import de.hsrm.vegetables.service.domain.dto.PurchaseDto;
import de.hsrm.vegetables.service.domain.dto.PurchasedItemDto;
import de.hsrm.vegetables.service.services.StockService;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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
        purchaseHistoryItem.setTotalPrice(purchaseDto.getTotalPrice());
        purchaseHistoryItem.setTotalVat(purchaseDto.getTotalVat());
        purchaseHistoryItem.setVatDetails(getVatDetails(purchaseDto));
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
        purchaseItem.setVat(purchasedItemDto.getVat());

        return purchaseItem;
    }

    public static Float getVatPaid(PurchasedItemDto purchasedItemDto) {
        float vatForItem = ((purchasedItemDto.getAmount() * purchasedItemDto.getPricePerUnit())
                / (1f + purchasedItemDto.getVat()) * purchasedItemDto.getVat());
        return StockService.round(vatForItem, 2);
    }

    private static List<VatDetailItem> getVatDetails(PurchaseDto purchaseDto) {
        // Get all distinct vat rates
        ArrayList<Float> distinctVatRates = new ArrayList<>();
        purchaseDto.getPurchasedItems()
                .forEach(purchasedItemDto -> {
                    if (!distinctVatRates.contains(purchasedItemDto.getVat())) {
                        distinctVatRates.add(purchasedItemDto.getVat());
                    }
                });

        return distinctVatRates.stream()
                .map(vat -> {
                    // Get all purchased items with specific vat
                    List<PurchasedItemDto> purchasedItemsWithVat = purchaseDto.getPurchasedItems()
                            .stream()
                            .filter(purchasedItemDto -> purchasedItemDto.getVat()
                                    .equals(vat))
                            .collect(Collectors.toList());

                    // Calculate vat amount for these items
                    Float amount = purchasedItemsWithVat.stream()
                            .map(PurchaseMapper::getVatPaid)
                            .reduce(0f, Float::sum);

                    VatDetailItem vatDetailItem = new VatDetailItem();
                    vatDetailItem.setVat(vat);
                    vatDetailItem.setAmount(StockService.round(amount, 2));
                    return vatDetailItem;
                })
                .collect(Collectors.toList());
    }

    public static List<VatDetailItem> getVatDetails(List<QuantitySoldItem> soldItems) {
        // Get all distinct vat rates
        ArrayList<Float> distinctVatRates = new ArrayList<>();

        soldItems.forEach(soldItem -> {
            if (!distinctVatRates.contains(soldItem.getVat())) {
                distinctVatRates.add(soldItem.getVat());
            }
        });

        return distinctVatRates.stream()
                .map(vat -> {
                    // Get all purchased items with specific vat
                    List<QuantitySoldItem> purchasedItemsWithVat = soldItems
                            .stream()
                            .filter(soldItem -> soldItem.getVat()
                                    .equals(vat))
                            .collect(Collectors.toList());

                    // Calculate vat amount for these items
                    Float amount = purchasedItemsWithVat.stream()
                            .map(QuantitySoldItem::getTotalVat)
                            .reduce(0f, Float::sum);

                    VatDetailItem vatDetailItem = new VatDetailItem();
                    vatDetailItem.setVat(vat);
                    vatDetailItem.setAmount(StockService.round(amount, 2));
                    return vatDetailItem;
                })
                .collect(Collectors.toList());
    }
}
