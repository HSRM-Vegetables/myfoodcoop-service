package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.PurchaseApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.*;
import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.domain.dto.PurchaseDto;
import de.hsrm.vegetables.service.domain.dto.StockDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.BadRequestError;
import de.hsrm.vegetables.service.exception.errors.http.UnauthorizedError;
import de.hsrm.vegetables.service.mapper.PurchaseMapper;
import de.hsrm.vegetables.service.security.UserPrincipal;
import de.hsrm.vegetables.service.services.BalanceService;
import de.hsrm.vegetables.service.services.PurchaseService;
import de.hsrm.vegetables.service.services.StockService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v2")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class PurchaseController implements PurchaseApi {

    @NonNull
    private final StockService stockService;

    @NonNull
    private final BalanceService balanceService;

    @NonNull
    private final PurchaseService purchaseService;

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<PurchaseResponse> purchaseFromStock(PurchaseRequest purchaseRequest) {
        String username = getUsernameFromSecurityContext();

        //Check Name is found
        BalanceDto balanceDto = balanceService.getBalance(username);

        // Get all associated items and update their quantities
        List<StockDto> stockItems = stockService.purchase(purchaseRequest.getItems());

        // Check that no item is out of stock
        Optional<StockDto> oneIsOutOfStock = stockItems.stream()
                .filter(stockItem -> stockItem.getStockStatus()
                        .equals(StockStatus.OUTOFSTOCK))
                .findFirst();
        if (oneIsOutOfStock.isPresent()) {
            throw new BadRequestError("Cannot purchase OUTOFSTOCK item with id " + oneIsOutOfStock.get()
                    .getId(), ErrorCode.ITEM_OUT_OF_STOCK);
        }

        // Calculate the total price of the cart
        Float totalPrice = StockService.calculatePrice(stockItems, purchaseRequest.getItems());

        // Get the balance
        balanceDto = balanceService.withdraw(balanceDto, totalPrice);

        PurchaseDto purchaseDto = purchaseService.purchaseItems(balanceDto, stockItems, purchaseRequest.getItems(), totalPrice);

        PurchaseResponse response = new PurchaseResponse();
        response.setName(username);
        response.setPrice(totalPrice);
        response.setBalance(balanceDto.getAmount());
        response.setId(purchaseDto.getId());

        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<PurchaseListResponse> purchaseGet() {
        String username = getUsernameFromSecurityContext();

        BalanceDto balanceDto = balanceService.getBalance(username);

        List<PurchaseDto> purchases = purchaseService.getPurchases(balanceDto);

        for (var purchase : purchases) {
            if (!balanceDto.getName()
                    .equals(purchase.getBalanceDto()
                            .getName())) {
                throw new UnauthorizedError("The associated name for that purchase does not match Header X-Username", ErrorCode.USERNAME_DOES_NOT_MATCH_PURCHASE);
            }
        }

        PurchaseListResponse purchaseListResponse = new PurchaseListResponse();
        purchaseListResponse.setPurchases(purchases.stream()
                .map(PurchaseMapper::purchaseDtoToPurchaseHistoryItem)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(purchaseListResponse);
    }

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<PurchaseHistoryItem> purchaseGetById(String purchaseId) {
        String username = getUsernameFromSecurityContext();

        PurchaseDto purchaseDto = purchaseService.getPurchase(purchaseId);

        if (!username.equals(purchaseDto.getBalanceDto()
                .getName())) {
            throw new UnauthorizedError("The associated name for that purchase does not match Header X-Username", ErrorCode.USERNAME_DOES_NOT_MATCH_PURCHASE);
        }

        PurchaseHistoryItem purchaseHistoryItem = PurchaseMapper.purchaseDtoToPurchaseHistoryItem(purchaseDto);
        return ResponseEntity.ok(purchaseHistoryItem);
    }

    private String getUsernameFromSecurityContext() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return userPrincipal.getUsername();
    }

}
