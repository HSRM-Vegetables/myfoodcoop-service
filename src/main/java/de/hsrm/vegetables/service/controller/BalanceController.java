package de.hsrm.vegetables.service.controller;

import de.hsrm.vegetables.Stadtgemuese_Backend.api.BalanceApi;
import de.hsrm.vegetables.Stadtgemuese_Backend.model.*;
import de.hsrm.vegetables.service.domain.dto.BalanceHistoryItemDto;
import de.hsrm.vegetables.service.domain.dto.UserDto;
import de.hsrm.vegetables.service.exception.ErrorCode;
import de.hsrm.vegetables.service.exception.errors.http.BadRequestError;
import de.hsrm.vegetables.service.mapper.BalanceMapper;
import de.hsrm.vegetables.service.repositories.OffsetLimit;
import de.hsrm.vegetables.service.security.UserPrincipal;
import de.hsrm.vegetables.service.services.BalanceService;
import de.hsrm.vegetables.service.services.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
            String userId, LocalDate fromDate, LocalDate toDate, Integer pageNumber, Integer pageSize) {

        LocalDate today = LocalDate.now();

        if (fromDate.isAfter(today) || toDate.isAfter(today)) {
            throw new BadRequestError("Report Date cannot be in the future", ErrorCode.REPORT_DATA_IN_FUTURE);
        }

        if (fromDate.isAfter(toDate)) {
            throw new BadRequestError("fromDate cannot be after toDate", ErrorCode.TO_DATE_AFTER_FROM_DATE);
        }

        OffsetDateTime fromDateConverted = OffsetDateTime.of(fromDate, LocalTime.MIN, ZoneOffset.UTC);
        OffsetDateTime toDateConverted = OffsetDateTime.of(toDate, LocalTime.MAX, ZoneOffset.UTC);

        UserDto userDto = userService.getUserById(userId);

        //
        // Query balance history items
        //

        Page<BalanceHistoryItemDto> balanceHistoryItemDtoPage = balanceService.findAllByUserDtoAndCreatedOnBetween(
                userDto, fromDateConverted, toDateConverted, new OffsetLimit(pageNumber, pageSize));

        List<BalanceHistoryItem> balanceHistoryItems = balanceHistoryItemDtoPage.stream()
                .map(BalanceMapper::balanceHistoryItemDtoToBalanceHistoryItem)
                .collect(Collectors.toList());

        //
        // Create response
        //

        Pagination pagination = new Pagination();
        pagination.setPageNumber(pageNumber);
        pagination.setPageSize(pageSize);
        pagination.setTotalPages(balanceHistoryItemDtoPage.getTotalPages());
        pagination.setTotalElements(balanceHistoryItemDtoPage.getTotalElements());

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

        balanceService.saveBalanceChange(userDto, BalanceChangeType.SET, request.getBalance());

        return ResponseEntity.ok(BalanceMapper.userDtoToBalanceResponse(userDto));
    }

    @Override
    @PreAuthorize("hasRole('MEMBER') and #userId == authentication.principal.id")
    public ResponseEntity<BalanceResponse> balanceTopup(String userId, BalanceAmountRequest request) {
        UserPrincipal userPrincipal = getUserPrincipalFromSecurityContext();

        UserDto userDto = userService.getUserById(userPrincipal.getId());
        userDto = userService.topup(userDto, request.getAmount());

        balanceService.saveBalanceChange(userDto, BalanceChangeType.TOPUP, request.getAmount());

        return ResponseEntity.ok(BalanceMapper.userDtoToBalanceResponse(userDto));
    }

    @Override
    @PreAuthorize("hasRole('MEMBER') and #userId == authentication.principal.id")
    public ResponseEntity<BalanceResponse> balanceWithdraw(String userId, BalanceAmountRequest request) {
        UserPrincipal userPrincipal = getUserPrincipalFromSecurityContext();

        UserDto userDto = userService.getUserById(userPrincipal.getId());
        userDto = userService.withdraw(userDto, request.getAmount());

        balanceService.saveBalanceChange(userDto, BalanceChangeType.WITHDRAW, request.getAmount());

        return ResponseEntity.ok(BalanceMapper.userDtoToBalanceResponse(userDto));
    }

    private UserPrincipal getUserPrincipalFromSecurityContext() {
        return (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
