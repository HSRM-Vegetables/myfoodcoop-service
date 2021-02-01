package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.BalanceApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceAmountRequest;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalancePatchRequest;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceResponse;
import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.exception.errors.http.NotFoundError;
import de.hsrm.vegetables.service.mapper.Mapper;
import de.hsrm.vegetables.service.security.UserPrincipal;
import de.hsrm.vegetables.service.services.BalanceService;
import de.hsrm.vegetables.service.services.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class BalanceController implements BalanceApi {

    @NonNull
    private final BalanceService balanceService;

    @NonNull
    private final UserService userService;

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<BalanceResponse> balanceGet() {
        String name = getUsernameFromSecurityContext();
        BalanceDto balanceDto = null;

        try {
            balanceDto = balanceService.getBalance(name);
        } catch (NotFoundError e) {
            balanceDto = balanceService.createEmptyBalance(name);
        }

        return ResponseEntity.ok(Mapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<BalanceResponse> balancePatch(BalancePatchRequest request) {
        String name = getUsernameFromSecurityContext();
        BalanceDto balanceDto = balanceService.upsert(name, request.getBalance());

        return ResponseEntity.ok(Mapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<BalanceResponse> balanceTopup(BalanceAmountRequest request) {
        String name = getUsernameFromSecurityContext();
        BalanceDto balanceDto = balanceService.topup(name, request.getAmount());

        return ResponseEntity.ok(Mapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<BalanceResponse> balanceWithdraw(BalanceAmountRequest request) {
        String name = getUsernameFromSecurityContext();
        BalanceDto balanceDto = balanceService.withdraw(name, request.getAmount());

        return ResponseEntity.ok(Mapper.balanceDtoToBalanceResponse(balanceDto));
    }

    private String getUsernameFromSecurityContext() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return userPrincipal.getUsername();
    }

    @Override
    @PreAuthorize("hasRole('TREASURER')")
    public ResponseEntity<BalanceResponse> userBalanceGet(String userId) {
        BalanceDto balanceDto = balanceService.getBalance(userService.getUserById(userId).getUsername());
        return ResponseEntity.ok(Mapper.balanceDtoToBalanceResponse(balanceDto));
    }

}
