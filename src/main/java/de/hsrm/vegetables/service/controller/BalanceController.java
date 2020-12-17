package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.BalanceApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceAmountRequest;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalancePatchRequest;

import java.util.HashMap;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceResponse;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.NotFoundError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
@CrossOrigin(origins = "*")
public class BalanceController implements BalanceApi {

    private final HashMap<String, Float> balances;

    public BalanceController() {
        this.balances = new HashMap<>();
    }

    @Override
    public ResponseEntity<BalanceResponse> balanceGet(String name) {
        BalanceResponse response = getBalanceResponse(name);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BalanceResponse> balancePatch(String name, BalancePatchRequest request) {
        balances.put(name, request.getBalance());

        BalanceResponse response = getBalanceResponse(name);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BalanceResponse> balanceTopup(String name, BalanceAmountRequest request) {
        if (!balances.containsKey(name)) {
            throw new NotFoundError("The balance for the given name was not found", ErrorCode.NO_BALANCE_FOUND);
        }

        balances.put(name, balances.get(name) + request.getAmount());

        BalanceResponse response = getBalanceResponse(name);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BalanceResponse> balanceWithdraw(String name, BalanceAmountRequest request) {
        if (!balances.containsKey(name)) {
            throw new NotFoundError("The balance for the given name was not found", ErrorCode.NO_BALANCE_FOUND);
        }

        balances.put(name, balances.get(name) - request.getAmount());

        BalanceResponse response = getBalanceResponse(name);
        return ResponseEntity.ok(response);
    }

    private BalanceResponse getBalanceResponse(String name) {
        BalanceResponse response = new BalanceResponse();

        if (balances.containsKey(name)) {
            response.balance(balances.get(name));
        } else {
            balances.put(name, 0f);
            response.balance(0f);
        }

        return response;
    }
}
