package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.PurchaseApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.PurchaseRequest;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.PurchaseResponse;
import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.services.BalanceService;
import de.hsrm.vegetables.service.services.StockService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class PurchaseController implements PurchaseApi {

    @NonNull
    private final StockService stockService;

    @NonNull
    private final BalanceService balanceService;

    @Override
    public ResponseEntity<PurchaseResponse> purchaseFromStock(String xUsername, PurchaseRequest purchaseRequest) {

        //Check Name is found
        balanceService.getBalance(xUsername);

        Float totalPrice = stockService.purchase(purchaseRequest.getItems());

        BalanceDto balanceDto = balanceService.withdraw(xUsername, totalPrice);

        PurchaseResponse response = new PurchaseResponse();
        response.setName(xUsername);
        response.setPrice(totalPrice);
        response.setBalance(balanceDto.getAmount());

        return ResponseEntity.ok(response);
    }

}
