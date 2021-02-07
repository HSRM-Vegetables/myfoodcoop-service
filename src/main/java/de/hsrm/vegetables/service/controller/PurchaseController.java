package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.PurchaseApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.PurchaseHistoryItem;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.PurchaseListResponse;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.PurchaseRequest;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.PurchaseResponse;
import de.hsrm.vegetables.service.domain.dto.PurchaseDto;
import de.hsrm.vegetables.service.domain.dto.StockDto;
import de.hsrm.vegetables.service.domain.dto.UserDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.UnauthorizedError;
import de.hsrm.vegetables.service.mapper.PurchaseMapper;
import de.hsrm.vegetables.service.security.UserPrincipal;
import de.hsrm.vegetables.service.services.PurchaseService;
import de.hsrm.vegetables.service.services.StockService;
import de.hsrm.vegetables.service.services.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class PurchaseController implements PurchaseApi {

    @NonNull
    private final StockService stockService;

    @NonNull
    private final UserService userService;

    @NonNull
    private final PurchaseService purchaseService;

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<PurchaseResponse> purchaseFromStock(PurchaseRequest purchaseRequest) {
        UserPrincipal userPrincipal = getUserPrincipalFromSecurityContext();

        // No need to query database for this userDto as we only need the primary key
        UserDto userDto = userService.getUserById(userPrincipal.getId());

        // Get all associated items and update their quantities
        List<StockDto> stockItems = stockService.reduceStockWithCartItems(purchaseRequest.getItems());

        // Calculate the total price of the cart
        Float totalPrice = StockService.calculatePrice(stockItems, purchaseRequest.getItems());

        // Calculate the total vat
        Float totalVat = StockService.calculateVatAmount(stockItems, purchaseRequest.getItems());

        // Withdraw the money from the users balance
        userDto = userService.withdraw(userDto, totalPrice);

        // Create the purchase
        PurchaseDto purchaseDto = purchaseService.purchaseItems(userDto, stockItems, purchaseRequest.getItems(), totalPrice, totalVat);

        PurchaseResponse response = new PurchaseResponse();
        response.setName(userPrincipal.getUsername());
        response.setPrice(totalPrice);
        response.setBalance(userDto.getBalance());
        response.setId(purchaseDto.getId());
        response.setTotalVat(totalVat);
        response.setUserId(userPrincipal.getId());
        response.setVatDetails(StockService.getVatDetails(stockItems, purchaseRequest.getItems()));

        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<PurchaseListResponse> purchaseGet() {
        UserPrincipal userPrincipal = getUserPrincipalFromSecurityContext();

        // No need to query database for this userDto as we only need the primary key
        UserDto userDto = new UserDto();
        userDto.setId(userPrincipal.getId());

        // Get all purchases from the user
        List<PurchaseDto> purchases = purchaseService.getPurchases(userDto);

        PurchaseListResponse purchaseListResponse = new PurchaseListResponse();
        purchaseListResponse.setPurchases(purchases.stream()
                .map(PurchaseMapper::purchaseDtoToPurchaseHistoryItem)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(purchaseListResponse);
    }

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<PurchaseHistoryItem> purchaseGetById(String purchaseId) {
        UserPrincipal userPrincipal = getUserPrincipalFromSecurityContext();

        PurchaseDto purchaseDto = purchaseService.getPurchase(purchaseId);

        // Check that the requested purchase blongs to the requesting user
        if (!userPrincipal.getId()
                .equals(purchaseDto.getUserDto()
                        .getId())) {
            throw new UnauthorizedError("The associated userId for that purchase does not match the id in the authorization token", ErrorCode.USER_DOES_NOT_MATCH_PURCHASE);
        }

        PurchaseHistoryItem purchaseHistoryItem = PurchaseMapper.purchaseDtoToPurchaseHistoryItem(purchaseDto);
        return ResponseEntity.ok(purchaseHistoryItem);
    }

    private UserPrincipal getUserPrincipalFromSecurityContext() {
        return (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

}
