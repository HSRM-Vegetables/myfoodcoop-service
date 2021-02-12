package de.hsrm.vegetables.my_food_coop_service.services;

import de.hsrm.vegetables.my_food_coop_service.model.DisposedItem;
import de.hsrm.vegetables.my_food_coop_service.model.VatDetailItem;
import de.hsrm.vegetables.my_food_coop_service.repositories.DisposedRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class DisposeService {

    public static List<VatDetailItem> getVatDetails(List<DisposedItem> disposedItems) {
        // Get all distinct vat rates
        ArrayList<Float> distinctVatRates = new ArrayList<>();

        disposedItems.forEach(soldItem -> {
            if (!distinctVatRates.contains(soldItem.getVat())) {
                distinctVatRates.add(soldItem.getVat());
            }
        });

        return distinctVatRates.stream()
                .map(vat -> {
                    // Get all purchased items with specific vat
                    List<DisposedItem> purchasedItemsWithVat = disposedItems
                            .stream()
                            .filter(soldItem -> soldItem.getVat()
                                    .equals(vat))
                            .collect(Collectors.toList());

                    // Calculate vat amount for these items
                    Float amount = purchasedItemsWithVat.stream()
                            .map(DisposedItem::getTotalVat)
                            .reduce(0f, Float::sum);

                    VatDetailItem vatDetailItem = new VatDetailItem();
                    vatDetailItem.setVat(vat);
                    vatDetailItem.setAmount(StockService.round(amount, 2));
                    return vatDetailItem;
                })
                .collect(Collectors.toList());
    }

}
