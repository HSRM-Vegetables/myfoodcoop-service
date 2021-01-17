package de.hsrm.vegetables.service.controller.v1;

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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class BalanceControllerV1 {

    @NonNull
    private final BalanceService balanceService;

    @GetMapping(
            value = "/balance/{name}",
            produces = {"application/json"}
    )
    public ResponseEntity<BalanceResponse> balanceGet(@PathVariable("name") String name) {
        BalanceDto balanceDto = null;

        try {
            balanceDto = balanceService.getBalance(name);
        } catch (NotFoundError e) {
            balanceDto = balanceService.createEmptyBalance(name);
        }

        return ResponseEntity.ok(Mapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @PatchMapping(
            value = "/balance/{name}",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public ResponseEntity<BalanceResponse> balancePatch(@PathVariable("name") String name, @Valid @RequestBody BalancePatchRequest request) {
        BalanceDto balanceDto = balanceService.upsert(name, request.getBalance());

        return ResponseEntity.ok(Mapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @PostMapping(
            value = "/balance/{name}/topup",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public ResponseEntity<BalanceResponse> balanceTopup(@PathVariable("name") String name, @Valid @RequestBody BalanceAmountRequest request) {
        BalanceDto balanceDto = balanceService.topup(name, request.getAmount());

        return ResponseEntity.ok(Mapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @PostMapping(
            value = "/balance/{name}/withdraw",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public ResponseEntity<BalanceResponse> balanceWithdraw(@PathVariable("name") String name, @Valid @RequestBody BalanceAmountRequest request) {
        BalanceDto balanceDto = balanceService.withdraw(name, request.getAmount());

        return ResponseEntity.ok(Mapper.balanceDtoToBalanceResponse(balanceDto));
    }

}
