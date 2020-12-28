package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.PurchaseApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.PurchaseRequest;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.PurchaseResponse;
import de.hsrm.vegetables.service.services.StockService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class PurchaseController implements PurchaseApi {

    @NonNull
    private final StockService stockService;

    @Override
    public ResponseEntity<PurchaseResponse> purchaseFromStock(String name, PurchaseRequest purchaseRequest) {

        // TODO: Check that user exists before next call

        Float totalPrice = stockService.purchase(purchaseRequest.getItems());

        // TODO: add balance handling

        PurchaseResponse response = new PurchaseResponse();
        response.setName(name);
        response.setPrice(totalPrice);
        response.setBalance(0f); // TODO: add balance handling

        return ResponseEntity.ok(response);
    }

}
