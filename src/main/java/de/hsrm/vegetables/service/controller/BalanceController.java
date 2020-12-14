package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.BalanceApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceAmountRequest;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceGetResponse;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalancePatchRequest;
import java.util.HashMap;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        BalanceGetResponse response = getBalanceResponse(name);
        return ResponseEntity.ok(response);
    }

    @Override
    @Valid
    public ResponseEntity<BalanceGetResponse> balancePatch(
            @PathVariable("name") String name,
            @Valid @RequestBody BalancePatchRequest request) {
        if (request.getBalance() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        balances.put(name, request.getBalance());

        BalanceGetResponse response = getBalanceResponse(name);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BalanceGetResponse> balanceTopup(String name, BalanceAmountRequest request) {
        if (request.getAmount() == null || !balances.containsKey(name) || request.getAmount() < 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        balances.put(name, balances.get(name) + request.getAmount());

        BalanceGetResponse response = getBalanceResponse(name);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BalanceGetResponse> balanceWithdraw(String name, BalanceAmountRequest request) {
        if (request.getAmount() == null || !balances.containsKey(name) || request.getAmount() < 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        balances.put(name, balances.get(name) - request.getAmount());

        BalanceGetResponse response = getBalanceResponse(name);
        return ResponseEntity.ok(response);
    }

    private BalanceGetResponse getBalanceResponse(String name) {
        BalanceGetResponse response = new BalanceGetResponse();

        if (balances.containsKey(name)) {
            response.balance(balances.get(name));
        } else {
            balances.put(name, 0f);
            response.balance(0f);
        }

        return response;
    }
}
