package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.BalanceApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceAmountRequest;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalancePatchRequest;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceResponse;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.Role;
import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.NotFoundError;
import de.hsrm.vegetables.service.exception.errors.http.UnauthorizedError;
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
    public ResponseEntity<BalanceResponse> userBalanceGet(String userId) {
        UserPrincipal userPrincipal = getUserPrincipalFromSecurityContext();

        // A non-Treasurer is only allowed to call this method for themselves
        if (!userPrincipal.getRoles().contains(Role.TREASURER)) {
            checkAccessingOwnUser(userPrincipal, userId);
        }

        BalanceDto balanceDto = balanceService.getBalance(userService.getUserById(userId).getUsername());
        return ResponseEntity.ok(Mapper.balanceDtoToBalanceResponse(balanceDto));
    }


    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<BalanceResponse> balancePatch(String userId, BalancePatchRequest request) {
        UserPrincipal userPrincipal = getUserPrincipalFromSecurityContext();

        // a user is only allowed to call this method for himself
        checkAccessingOwnUser(userPrincipal, userId);

        BalanceDto balanceDto = balanceService.upsert(userPrincipal.getUsername(), request.getBalance());

        return ResponseEntity.ok(Mapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<BalanceResponse> balanceTopup(String userId, BalanceAmountRequest request) {
        UserPrincipal userPrincipal = getUserPrincipalFromSecurityContext();

        // a user is only allowed to call this method for himself
        checkAccessingOwnUser(userPrincipal, userId);

        BalanceDto balanceDto = balanceService.topup(userPrincipal.getUsername(), request.getAmount());

        return ResponseEntity.ok(Mapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<BalanceResponse> balanceWithdraw(String userId, BalanceAmountRequest request) {
        UserPrincipal userPrincipal = getUserPrincipalFromSecurityContext();

        // a user is only allowed to call this method for himself
        checkAccessingOwnUser(userPrincipal, userId);

        BalanceDto balanceDto = balanceService.withdraw(userPrincipal.getUsername(), request.getAmount());

        return ResponseEntity.ok(Mapper.balanceDtoToBalanceResponse(balanceDto));
    }

    private UserPrincipal getUserPrincipalFromSecurityContext() {
        return (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private void checkAccessingOwnUser(UserPrincipal userPrincipal, String userId) {
        if (!userId.equals(userPrincipal.getId())) {
            throw new UnauthorizedError("Access Denied", ErrorCode.METHOD_ONLY_ALLOWED_FOR_OWN_USER);
        }
    }



}
