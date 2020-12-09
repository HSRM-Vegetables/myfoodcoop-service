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

    private final HashMap<String, BigDecimal> balances;

    public BalanceController() {
        this.balances = new HashMap<>();
    }

    @Override
    public ResponseEntity<BalanceGetResponse> balanceGet(String name) {
        BalanceGetResponse response = new BalanceGetResponse();

        if (balances.containsKey(name)) {
            response.balance(balances.get(name));
        } else {
            response.balance(new BigDecimal(0));
        }

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> balancePost(String name, BalancePostRequest request) {
        balances.put(name, request.getBalance());
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
