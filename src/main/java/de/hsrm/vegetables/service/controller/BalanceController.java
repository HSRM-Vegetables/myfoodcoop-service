package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.BalanceApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.*;
import de.hsrm.vegetables.service.domain.dto.BalanceDto;
import de.hsrm.vegetables.service.domain.dto.BalanceHistoryItemDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.NotFoundError;
import de.hsrm.vegetables.service.exception.errors.http.UnauthorizedError;
import de.hsrm.vegetables.service.mapper.BalanceMapper;
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

import java.util.List;
import java.util.stream.Collectors;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class BalanceController implements BalanceApi {

    @NonNull
    private final BalanceService balanceService;

    @NonNull
    private final UserService userService;

    @Override
    @PreAuthorize("hasRole('MEMBER') and (#userId == authentication.principal.id or hasRole('TREASURER'))")
    public ResponseEntity<BalanceResponse> userBalanceGet(String userId) {
        BalanceDto balanceDto = balanceService.getBalance(userService.getUserById(userId)
                .getUsername());
        return ResponseEntity.ok(BalanceMapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @Override
    @PreAuthorize("hasRole('MEMBER') and (#userId == authentication.principal.id or hasRole('TREASURER'))")
    public ResponseEntity<BalanceHistoryResponse> balanceHistoryGet(String userId, Integer offset, Integer limit) {
        BalanceDto balanceDto = balanceService.getBalance(userService.getUserById(userId).getUsername());

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

        Pagination pagination = new Pagination();
        pagination.setOffset(offset);
        pagination.setLimit(limit);
        pagination.setTotal(balanceHistoryItems.size());
        balanceHistoryResponse.setPagination(pagination);

        return ResponseEntity.ok(balanceHistoryResponse);
    }


    @Override
    @PreAuthorize("hasRole('MEMBER') and #userId == authentication.principal.id")
    public ResponseEntity<BalanceResponse> balancePatch(String userId, BalancePatchRequest request) {
        UserPrincipal userPrincipal = getUserPrincipalFromSecurityContext();

        BalanceDto balanceDto = balanceService.upsert(userPrincipal.getUsername(), request.getBalance());

        return ResponseEntity.ok(BalanceMapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @Override
    @PreAuthorize("hasRole('MEMBER') and #userId == authentication.principal.id")
    public ResponseEntity<BalanceResponse> balanceTopup(String userId, BalanceAmountRequest request) {
        UserPrincipal userPrincipal = getUserPrincipalFromSecurityContext();

        BalanceDto balanceDto = balanceService.topup(userPrincipal.getUsername(), request.getAmount());

        return ResponseEntity.ok(BalanceMapper.balanceDtoToBalanceResponse(balanceDto));
    }

    @Override
    @PreAuthorize("hasRole('MEMBER') and #userId == authentication.principal.id")
    public ResponseEntity<BalanceResponse> balanceWithdraw(String userId, BalanceAmountRequest request) {
        UserPrincipal userPrincipal = getUserPrincipalFromSecurityContext();

        BalanceDto balanceDto = balanceService.withdraw(userPrincipal.getUsername(), request.getAmount());

        return ResponseEntity.ok(BalanceMapper.balanceDtoToBalanceResponse(balanceDto));
    }

    private UserPrincipal getUserPrincipalFromSecurityContext() {
        return (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

}
