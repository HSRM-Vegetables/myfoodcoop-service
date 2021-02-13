package de.hsrm.vegetables.my_food_coop_service.controller;

import de.hsrm.vegetables.my_food_coop_service.api.BalanceApi;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.BalanceHistoryItemDto;
import de.hsrm.vegetables.my_food_coop_service.domain.dto.UserDto;
import de.hsrm.vegetables.my_food_coop_service.mapper.BalanceMapper;
import de.hsrm.vegetables.my_food_coop_service.model.*;
import de.hsrm.vegetables.my_food_coop_service.repositories.OffsetLimit;
import de.hsrm.vegetables.my_food_coop_service.security.UserPrincipal;
import de.hsrm.vegetables.my_food_coop_service.services.BalanceService;
import de.hsrm.vegetables.my_food_coop_service.services.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class BalanceController implements BalanceApi {

    @NonNull
    private final UserService userService;

    @NonNull
    private final BalanceService balanceService;

    @Override
    @PreAuthorize("hasRole('MEMBER') and (#userId == authentication.principal.id or hasRole('TREASURER'))")
    public ResponseEntity<BalanceResponse> userBalanceGet(String userId) {
        UserDto userDto = userService.getUserById(userId);
        return ResponseEntity.ok(BalanceMapper.userDtoToBalanceResponse(userDto));
    }

    @Override
    @PreAuthorize("hasRole('MEMBER') and (#userId == authentication.principal.id or hasRole('TREASURER'))")
    public ResponseEntity<BalanceHistoryResponse> userBalanceHistoryGet(
            String userId, LocalDate fromDate, LocalDate toDate, Integer offset, Integer limit) {

        UserDto userDto = userService.getUserById(userId);

        // Query balance history items

        Page<BalanceHistoryItemDto> balanceHistoryItemDtoPage = balanceService.findAllByUserDtoAndCreatedOnBetween(
                userDto, fromDate, toDate, offset, limit);

        List<BalanceHistoryItem> balanceHistoryItems = balanceHistoryItemDtoPage.stream()
                .map(BalanceMapper::balanceHistoryItemDtoToBalanceHistoryItem)
                .collect(Collectors.toList());

        // Create response

        Pagination pagination = new Pagination();
        pagination.setOffset(offset);
        pagination.setLimit(limit);
        pagination.setTotal(balanceHistoryItemDtoPage.getTotalElements());

        BalanceHistoryResponse balanceHistoryResponse = new BalanceHistoryResponse();
        balanceHistoryResponse.setBalanceHistoryItems(balanceHistoryItems);
        balanceHistoryResponse.setPagination(pagination);

        return ResponseEntity.ok(balanceHistoryResponse);
    }

    @Override
    @PreAuthorize("hasRole('MEMBER') and #userId == authentication.principal.id")
    public ResponseEntity<BalanceResponse> balancePatch(String userId, BalancePatchRequest request) {
        UserPrincipal userPrincipal = getUserPrincipalFromSecurityContext();

        UserDto userDto = userService.getUserById(userPrincipal.getId());
        userDto = userService.setBalance(userDto, request.getBalance());

        balanceService.saveBalanceChange(userDto, OffsetDateTime.now(), null,
                BalanceChangeType.SET, request.getBalance());

        return ResponseEntity.ok(BalanceMapper.userDtoToBalanceResponse(userDto));
    }

    @Override
    @PreAuthorize("hasRole('MEMBER') and #userId == authentication.principal.id")
    public ResponseEntity<BalanceResponse> balanceTopup(String userId, BalanceAmountRequest request) {
        UserPrincipal userPrincipal = getUserPrincipalFromSecurityContext();

        UserDto userDto = userService.getUserById(userPrincipal.getId());
        userDto = userService.topup(userDto, request.getAmount());

        balanceService.saveBalanceChange(userDto, OffsetDateTime.now(), null,
                BalanceChangeType.TOPUP, request.getAmount());

        return ResponseEntity.ok(BalanceMapper.userDtoToBalanceResponse(userDto));
    }

    @Override
    @PreAuthorize("hasRole('MEMBER') and #userId == authentication.principal.id")
    public ResponseEntity<BalanceResponse> balanceWithdraw(String userId, BalanceAmountRequest request) {
        UserPrincipal userPrincipal = getUserPrincipalFromSecurityContext();

        UserDto userDto = userService.getUserById(userPrincipal.getId());
        userDto = userService.withdraw(userDto, request.getAmount());

        balanceService.saveBalanceChange(userDto, OffsetDateTime.now(), null,
                BalanceChangeType.WITHDRAW, request.getAmount());

        return ResponseEntity.ok(BalanceMapper.userDtoToBalanceResponse(userDto));
    }

    private UserPrincipal getUserPrincipalFromSecurityContext() {
        return (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
