package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.BalanceApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;

@RestController
@RequestMapping("/v1")
@CrossOrigin(origins = "*")
public class BalanceController implements BalanceApi {

    private final HashMap<String, Float> balances;

    public BalanceController() {
        this.balances = new HashMap<>();
    }

    @Override
    public ResponseEntity<BalanceGetResponse> balanceGet(String name) {
        BalanceGetResponse response = new BalanceGetResponse();

        if (balances.containsKey(name)) {
            response.balance(balances.get(name));
        } else {
            balances.put(name, 0f);
            response.balance(0f);
        }

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> balancePost(String name, BalancePostRequest request) {
        if (request.getBalance() != null && request.getBalanceDifference() != null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (request.getBalance() != null) {
            balances.put(name, request.getBalance());
        } else if (request.getBalanceDifference() != null) {
            Float currentBalance = balances.get(name);
            Float newBalance = currentBalance + request.getBalanceDifference();
            balances.put(name, newBalance);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
