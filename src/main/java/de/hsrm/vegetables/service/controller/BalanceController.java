package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.BalanceApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceAmountRequest;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceHistoryResponse;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalancePatchRequest;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.BalanceResponse;
import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.domain.dto.BalanceHistoryItemDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.NotFoundError;
import de.hsrm.vegetables.service.exception.errors.http.UnauthorizedError;
import de.hsrm.vegetables.service.mapper.BalanceMapper;
import de.hsrm.vegetables.service.security.UserPrincipal;
import de.hsrm.vegetables.service.services.BalanceService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class BalanceController implements BalanceApi {

    @NonNull
    private final BalanceService balanceService;

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

        return ResponseEntity.ok(BalanceMapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<BalanceHistoryResponse> balanceHistoryGet(Integer offset, Integer limit) {
        String name = getUsernameFromSecurityContext();
        BalanceDto balanceDto = null;

        try {
            balanceDto = balanceService.getBalance(name);
        } catch (NotFoundError e) {
            balanceDto = balanceService.createEmptyBalance(name);
        }

        List<BalanceHistoryItemDto> balanceHistoryItems = balanceService.getBalanceHistoryItems(balanceDto);

        for (var balanceHistoryItem : balanceHistoryItems) {
            if (!balanceDto.getName().equals(balanceHistoryItem.getBalanceDto().getName())) {
                throw new UnauthorizedError("The associated name for that balance history item does not match Header X-Username",
                        ErrorCode.USERNAME_DOES_NOT_MATCH_PURCHASE);
            }
        }

        BalanceHistoryResponse balanceHistoryResponse = new BalanceHistoryResponse();
        balanceHistoryResponse.setBalanceHistoryItems(balanceHistoryItems.stream()
                .map(BalanceMapper::balanceHistoryItemDtoToBalanceHistoryItem)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(balanceHistoryResponse);
    }

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<BalanceResponse> balancePatch(BalancePatchRequest request) {
        String name = getUsernameFromSecurityContext();
        BalanceDto balanceDto = balanceService.upsert(name, request.getBalance());

        return ResponseEntity.ok(BalanceMapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<BalanceResponse> balanceTopup(BalanceAmountRequest request) {
        String name = getUsernameFromSecurityContext();
        BalanceDto balanceDto = balanceService.topup(name, request.getAmount());

        return ResponseEntity.ok(BalanceMapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @Override
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<BalanceResponse> balanceWithdraw(BalanceAmountRequest request) {
        String name = getUsernameFromSecurityContext();
        BalanceDto balanceDto = balanceService.withdraw(name, request.getAmount());

        return ResponseEntity.ok(BalanceMapper.balanceDtoToBalanceResponse(balanceDto));
    }

    private String getUsernameFromSecurityContext() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return userPrincipal.getUsername();
    }

}
