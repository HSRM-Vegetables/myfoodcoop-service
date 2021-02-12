package de.hsrm.vegetables.my_food_coop_service.controller;

import de.hsrm.vegetables.my_food_coop_service.api.ReportsApi;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.PurchaseDto;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.StockDto;
import de.hsrm.vegetables.my_food_coop_service.exception.ErrorCode;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.http.BadRequestError;
import de.hsrm.vegetables.my_food_coop_service.mapper.PurchaseMapper;
import de.hsrm.vegetables.my_food_coop_service.model.*;
import de.hsrm.vegetables.my_food_coop_service.services.PurchaseService;
import de.hsrm.vegetables.my_food_coop_service.services.StockService;
import de.hsrm.vegetables.my_food_coop_service.services.UserService;
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
import java.util.List;
import java.util.Optional;
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
        response.setVatDetails(PurchaseMapper.getVatDetails(soldItems));
        Float totalVat = soldItems.stream()
                .map(QuantitySoldItem::getTotalVat)
                .reduce(0f, Float::sum);
        Float grossAmount = soldItems.stream()
                .map(QuantitySoldItem::getGrossAmount)
                .reduce(0f, Float::sum);
        response.setTotalVat(StockService.round(totalVat, 2));
        response.setGrossAmount(StockService.round(grossAmount, 2));
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
        List<QuantitySoldItem> soldItems = new ArrayList<>();

        // Collect all sold items and aggregate them by id and vat
        purchases.forEach(purchaseDto -> purchaseDto.getPurchasedItems()
                .forEach(purchasedItemDto -> {
                    String stockId = purchasedItemDto.getStockDto()
                            .getId();
                    Float vat = purchasedItemDto.getVat();
                    Float vatPaid = PurchaseMapper.getVatPaid(purchasedItemDto);
                    Float grossPrice = StockService.round(purchasedItemDto.getPricePerUnit() * purchasedItemDto.getAmount(), 2);

                    // Check if we've already seen an item with this id and this vat
                    Optional<QuantitySoldItem> associatedSoldItem = soldItems.stream()
                            .filter(quantitySoldItem -> quantitySoldItem.getId()
                                    .equals(stockId) && quantitySoldItem.getVat()
                                    .equals(vat))
                            .findFirst();

                    if (associatedSoldItem.isEmpty()) {
                        // No item for this vat/id combination found yet -> create one
                        QuantitySoldItem soldItem = new QuantitySoldItem();
                        soldItem.setQuantitySold(purchasedItemDto.getAmount());
                        soldItem.setId(stockId);
                        soldItem.setUnitType(purchasedItemDto.getUnitType());
                        soldItem.setFromDate(fromDate);
                        soldItem.setToDate(toDate);
                        soldItem.setVat(vat);
                        soldItem.setTotalVat(vatPaid);
                        soldItem.setGrossAmount(grossPrice);
                        soldItems.add(soldItem);
                    } else {
                        QuantitySoldItem soldItem = associatedSoldItem.get();
                        // update amount
                        soldItem.setQuantitySold(soldItem.getQuantitySold() + purchasedItemDto.getAmount());
                        // update tax
                        soldItem.setTotalVat(StockService.round(soldItem.getTotalVat() + vatPaid, 2));
                        // update gross price
                        soldItem.setGrossAmount(StockService.round(soldItem.getGrossAmount() + grossPrice, 2));
                    }
                }));

        // additionally collect the name of each item from the stock
        soldItems.forEach(soldItem -> {
            StockDto stockItem = stockService.getById(soldItem.getId());
            soldItem.setName(stockItem.getName());
        });

        return soldItems;
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
                            item.setBalance(user.getBalance());
                            return item;
                        })
                        .collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }
}