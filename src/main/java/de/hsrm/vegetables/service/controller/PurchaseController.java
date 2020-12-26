package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.PurchaseApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.PurchaseRequest;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.PurchaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
@CrossOrigin(origins = "*")
public class PurchaseController implements PurchaseApi {

    @Override
    public ResponseEntity<PurchaseResponse> purchaseFromStock(String name, PurchaseRequest purchaseRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
