package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.ReportsApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.*;
import de.hsrm.vegetables.service.domain.dto.PurchaseDto;
import de.hsrm.vegetables.service.domain.dto.StockDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.BadRequestError;
import de.hsrm.vegetables.service.services.BalanceService;
import de.hsrm.vegetables.service.services.PurchaseService;
import de.hsrm.vegetables.service.services.StockService;
import de.hsrm.vegetables.service.services.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class ReportsController implements ReportsApi {

    @NonNull
    private PurchaseService purchaseService;

    @NonNull
    private final StockService stockService;

    @NonNull
    private UserService userService;

    @NonNull
    private BalanceService balanceService;

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<QuantitySoldList> soldItems(LocalDate fromDate, LocalDate toDate) {
        LocalDate today = LocalDate.now();
        if (fromDate.isAfter(today) || toDate.isAfter(today)) {
            throw new BadRequestError("Report Date cannot be in the future", ErrorCode.REPORT_DATA_IN_FUTURE);
        }

        if (fromDate.isAfter(toDate)) {
            throw new BadRequestError("fromDate cannot be after toDate", ErrorCode.TO_DATE_AFTER_FROM_DATE);
        }

        List<QuantitySoldItem> soldItems = getSoldItems(fromDate, toDate);
        QuantitySoldList response = new QuantitySoldList();
        response.setItems(soldItems);

        return ResponseEntity.ok(response);
    }

    /**
     * Find multiple purchases between Dates
     *
     * @param fromDate time window from offsetDateTime where item was purchased
     * @param toDate   time window to offsetDateTime where item was purchased
     * @return All purchases between fromDate and toDate
     */
    private List<QuantitySoldItem> getSoldItems(LocalDate fromDate, LocalDate toDate) {
        // Local Dates only contain date information and are missing time information.
        // Convert the LocalDate to a timestamp with the options specified below.
        OffsetDateTime fromDateConverted = OffsetDateTime.of(fromDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime toDateConverted = OffsetDateTime.of(toDate, LocalTime.MAX, ZoneOffset.UTC);

        List<PurchaseDto> purchases = purchaseService.findAllByCreatedOnBetween(fromDateConverted, toDateConverted);
        HashMap<String, QuantitySoldItem> purchaseQuantityByStockId = new HashMap<String, QuantitySoldItem>();

        // iterate over each purchaseItem and collect the amount each purchaseItem was purchased
        purchases.forEach(purchase -> {
            purchase.getPurchasedItems()
                    .forEach(purchaseItem -> {
                        String stockId = purchaseItem.getStockDto()
                                .getId();
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
                            quantitySoldItem.setFromDate(fromDate);
                            quantitySoldItem.setToDate(toDate);
                        }

                        purchaseQuantityByStockId.put(stockId, quantitySoldItem);
                    });
        });

        // additionally collect the name of each item from the stock
        purchaseQuantityByStockId.forEach((stockId, quantitySoldItem) -> {
            StockDto stockItem = stockService.getById(stockId);
            quantitySoldItem.setName(stockItem.getName());
        });

        return new ArrayList<>(purchaseQuantityByStockId.values());
    }

    @Override
    @PreAuthorize("hasRole('TREASURER')")
    public ResponseEntity<BalanceOverviewList> balanceOverview(DeleteFilter deleted) {
        BalanceOverviewList response = new BalanceOverviewList();

        response.setUsers(
            userService.getAll(deleted)
            .stream()
            .map(user -> {
                BalanceOverviewItem item = new BalanceOverviewItem();
                item.setId(user.getId());
                item.setUsername(user.getUsername());
                item.setMemberId(user.getMemberId());
                item.setIsDeleted(user.isDeleted());
                item.setBalance(balanceService
                    .getBalance(user.getUsername())
                    .getAmount());
                return item;
            })
            .collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }
}
