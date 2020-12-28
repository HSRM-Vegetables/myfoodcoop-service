package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.BalanceApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceAmountRequest;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalancePatchRequest;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceResponse;
import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.exception.errors.http.NotFoundError;
import de.hsrm.vegetables.service.mapper.Mapper;
import de.hsrm.vegetables.service.services.BalanceService;
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
public class BalanceController implements BalanceApi {

    @NonNull
    private final BalanceService balanceService;

    @Override
    public ResponseEntity<BalanceResponse> balanceGet(String name) {
        BalanceDto balanceDto = null;

        try {
            balanceDto = balanceService.getBalance(name);
        } catch (NotFoundError e) {
            balanceDto = balanceService.createEmptyBalance(name);
        }

        return ResponseEntity.ok(Mapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @Override
    public ResponseEntity<BalanceResponse> balancePatch(String name, BalancePatchRequest request) {
        BalanceDto balanceDto = balanceService.upsert(name, request.getBalance());

        return ResponseEntity.ok(Mapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @Override
    public ResponseEntity<BalanceResponse> balanceTopup(String name, BalanceAmountRequest request) {
        BalanceDto balanceDto = balanceService.topup(name, request.getAmount());

        return ResponseEntity.ok(Mapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @Override
    public ResponseEntity<BalanceResponse> balanceWithdraw(String name, BalanceAmountRequest request) {
        BalanceDto balanceDto = balanceService.withdraw(name, request.getAmount());

        return ResponseEntity.ok(Mapper.balanceDtoToBalanceResponse(balanceDto));
    }

}
