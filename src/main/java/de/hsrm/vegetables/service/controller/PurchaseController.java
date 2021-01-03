package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.PurchaseApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.PurchaseHistoryItem;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.PurchaseListResponse;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.PurchaseRequest;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.PurchaseResponse;
import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.domain.dto.PurchaseDto;
import de.hsrm.vegetables.service.domain.dto.StockDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.UnauthorizedError;
import de.hsrm.vegetables.service.mapper.PurchaseMapper;
import de.hsrm.vegetables.service.services.BalanceService;
import de.hsrm.vegetables.service.services.PurchaseService;
import de.hsrm.vegetables.service.services.StockService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1")
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
    public ResponseEntity<PurchaseResponse> purchaseFromStock(String xUsername, PurchaseRequest purchaseRequest) {
        //Check Name is found
        BalanceDto balanceDto = balanceService.getBalance(xUsername);

        // Get all associated items and update their quantities
        List<StockDto> stockItems = stockService.purchase(purchaseRequest.getItems());

        // Calculate the total price of the cart
        Float totalPrice = StockService.calculatePrice(stockItems, purchaseRequest.getItems());

        // Get the balance
        balanceDto = balanceService.withdraw(balanceDto, totalPrice);

        PurchaseDto purchaseDto = purchaseService.purchaseItems(balanceDto, stockItems, purchaseRequest.getItems(), totalPrice);

        PurchaseResponse response = new PurchaseResponse();
        response.setName(xUsername);
        response.setPrice(totalPrice);
        response.setBalance(balanceDto.getAmount());
        response.setId(purchaseDto.getId());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<PurchaseListResponse> purchaseGet(String xUsername) {

        BalanceDto balanceDto = balanceService.getBalance(xUsername);

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
    public ResponseEntity<PurchaseHistoryItem> purchaseGetById(String xUsername, String purchaseId) {
        BalanceDto balanceDto = balanceService.getBalance(xUsername);

        PurchaseDto purchaseDto = purchaseService.getPurchase(purchaseId);

        if (!balanceDto.getName()
                .equals(purchaseDto.getBalanceDto()
                        .getName())) {
            throw new UnauthorizedError("The associated name for that purchase does not match Header X-Username", ErrorCode.USERNAME_DOES_NOT_MATCH_PURCHASE);
        }

        PurchaseHistoryItem purchaseHistoryItem = PurchaseMapper.purchaseDtoToPurchaseHistoryItem(purchaseDto);
        return ResponseEntity.ok(purchaseHistoryItem);
    }

}
